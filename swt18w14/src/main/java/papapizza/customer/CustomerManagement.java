package papapizza.customer;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.javamoney.moneta.Money;
import org.salespointframework.accountancy.Accountancy;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.quantity.Quantity;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.salespointframework.time.BusinessTime;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import papapizza.catalog.Item;
import papapizza.catalog.ProductCatalog;
import papapizza.catalog.ProductType;

import static org.salespointframework.core.Currencies.EURO;

@Service
@Transactional
public class CustomerManagement {

	private final CustomerRepository customers;
	private final BusinessTime businessTime;
	private final Inventory<InventoryItem> inventory;
	private final ProductCatalog catalog;
	private final Accountancy accountancy;
	private Customer verifiedCustomer;


	CustomerManagement(CustomerRepository customers, BusinessTime businessTime, Inventory<InventoryItem> inventory,
					   ProductCatalog catalog, Accountancy accountancy) {

		Assert.notNull(customers, "CustomerRepository must not be null!");
		Assert.notNull(businessTime, "Businesstime must not be null!");
		Assert.notNull(accountancy, "Accountancy must not be null!");
		Assert.notNull(inventory, "Inventory must not be null!");
		Assert.notNull(catalog, "Catalog must not be null!");

		this.accountancy = accountancy;
		this.customers = customers;
		this.businessTime = businessTime;
		this.inventory = inventory;
		this.catalog = catalog;
	}


	public Customer createCustomer(CustomerRegistrationForm form) {

		Assert.notNull(form, "Registration form must not be null!");

		Customer customer = new Customer(form.getFirstname(), form.getLastname(), form.getTel());
		customer.setAddress(form.getAddress());

		return customers.save(customer);
	}

	public Streamable<Customer> findAll() {
		return Streamable.of(customers.findAll());
	}



	public Customer findCustomer(long id) {

		return customers.findById(id).get();
	}

	public Optional<Customer> findByTel(String tel) {


		Iterator<Customer> customerIterator = findAll().iterator();

		while(customerIterator.hasNext()) {


			Customer customer = customerIterator.next();

			if(customer.getTel().equals(tel)) {

				return Optional.of(customer);
			}
		}

		return Optional.empty();
	}

	public void newTan(Customer customer) {
		customer.setTan(tanGenerator());
		customers.save(customer);
	}



	private String tanGenerator() {

		int leftLimit = 65;
		int rightLimit = 90;
		int targetStringLength = 11;
		Random random = new Random();
		StringBuilder buffer = new StringBuilder(targetStringLength);
		for (int i = 0; i < targetStringLength; i++) {
			int randomLimitedInt = leftLimit + (int)
					(random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}
		return buffer.toString();
	}

	public RedirectAttributes editCustomer(Customer customer, CustomerEditForm form, RedirectAttributes redirectAttributes) {

			Assert.notNull(form, "Edit form must not be null!");

		customer.setFirstname(form.getFirstname());
		customer.setLastname(form.getLastname());
		customer.setAddress(form.getAddress());

		if(!form.getTel().equals(customer.getTel())){
			if(findByTel(form.getTel()).isPresent()){
				redirectAttributes.addFlashAttribute("telExists", true);
			} else{	customer.setTel(form.getTel()); }
		}

		long unreturnable;

		if(form.getReturnDiningSet()>0){ unreturnable = removeDiningSets(form.getReturnDiningSet(), customer); }
		else { unreturnable = 0; }

		redirectAttributes.addFlashAttribute("unreturned", unreturnable);

		customers.save(customer);

		return redirectAttributes;

	}

	public void removeCustomer(Customer customer){
		customers.delete(customer);
	}

	public void addDiningSets(long l, Customer customer) {

		ConcurrentHashMap<LocalDate, Long> rented = customer.getDiningSets();
		LocalDate now = businessTime.getTime().toLocalDate().plusWeeks(4);

		if (rented.keySet().iterator().hasNext()) {
			LocalDate newest = LocalDate.MIN;

			Iterator<LocalDate> date = rented.keySet().iterator();

			while(date.hasNext()) {
				LocalDate current = date.next();
				if(current.isAfter(newest)) {
					newest = current;
				}
			}

			if (newest.equals(now)) {
				rented.put(now, rented.get(newest) + l);

			} else {
				rented.put(businessTime.getTime().plusWeeks(4).toLocalDate(), l);
			}
		} else {
			rented.put(businessTime.getTime().plusWeeks(4).toLocalDate(), l);
		}
		customers.save(customer);
	}

	public long removeDiningSets(long l, Customer customer) {

		long before = customer.getCountDiningSets();
		AtomicLong p_sets = new AtomicLong();

		ConcurrentHashMap<LocalDate, Long> rented = customer.getDiningSets();
		LocalDate now = businessTime.getTime().toLocalDate().plusWeeks(4);

		if (l > 0) {

			rented.forEach((time, count) -> {			//wirft zu alte rückgabeoptionen raus

				if (time.compareTo(now) < 0) {
					rented.remove(time);
					p_sets.getAndIncrement();
				}
			});
			if(rented.size()>0) {

				while (l > 0 && rented.size()>0) {
					Iterator<LocalDate> it = rented.keySet().iterator();
					LocalDate eldest = LocalDate.MIN;

					if (it.hasNext()) {

						while (it.hasNext()) {

							LocalDate current = it.next();

							if (current.isAfter(eldest)) {
								eldest = current;
							}
						}
					}

					if (rented.get(eldest) == l) {
						rented.remove(eldest);
						l = 0;
					} else if (rented.get(eldest) > l) {
						rented.put(eldest, rented.get(eldest) - l);
						l = 0;
					} else {
						l = l - rented.get(eldest);
						rented.remove(eldest);
					}
				}
			}
		}

		customers.save(customer);

		long result = before - customer.getCountDiningSets() - p_sets.get();

		if(result==0){
			return l;
		}

		ArrayList<Item> items = new ArrayList<>();
		catalog.findByType(ProductType.DINING_SET).forEach(citem -> {items.add(citem);});
		InventoryItem inventoryItem = inventory.findByProduct(items.get(0)).get();
		inventoryItem.increaseQuantity(Quantity.of(result));
		inventory.save(inventory.findByProduct(items.get(0)).get());
		Money m = Money.of(result * -15, EURO);
		accountancy.add(new AccountancyEntry(m, "returned DiningSets from " + customer.getFullName()));

		return l;
	}

	public Customer getVerifiedCustomer() {return verifiedCustomer;}

	public void setVerifiedCustomer(Customer verifiedCustomer) { this.verifiedCustomer = verifiedCustomer; }
}