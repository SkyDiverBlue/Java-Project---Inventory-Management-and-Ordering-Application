package papapizza.customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


import org.salespointframework.accountancy.Accountancy;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Streamable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import papapizza.catalog.ProductCatalog;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;


@SpringBootTest
public class CustomerManagementTests {

	@Autowired
	CustomerManagement management;

	@Autowired
	CustomerRepository customers;

	@Autowired
	CustomerController controller;

	@Autowired
	Inventory<InventoryItem> inventory;

	@Autowired
	ProductCatalog catalog;

	@Autowired
	Accountancy accountancy;

	@Autowired
	BusinessTime businessTime;

	private Customer customer;
	private Customer customer2;

	private CustomerRegistrationForm registrationForm;
	private CustomerRegistrationForm registrationForm2;
	private CustomerEditForm editForm;

	@BeforeEach
	public void setUp(){
		management.setVerifiedCustomer(null);
		businessTime.reset();
		customers.deleteAll();
		customer = new Customer("John", "Smith", "1234");
		customer.setAddress("Raibow Road");
		customers.save(customer);
		customer2 = new Customer("Max", "Mustermann", "Mensch");
		customer2.setAddress("Raibow Road");
		customers.save(customer2);

		registrationForm = mock(CustomerRegistrationForm.class);
		given(registrationForm.getFirstname()).willReturn("Jane");
		given(registrationForm.getLastname()).willReturn("Doe");
		given(registrationForm.getTel()).willReturn("0491");
		given(registrationForm.getAddress()).willReturn("Rainbow Road");

		registrationForm2 = mock(CustomerRegistrationForm.class);
		given(registrationForm2.getFirstname()).willReturn("Kevin");
		given(registrationForm2.getLastname()).willReturn("Tracy");
		given(registrationForm2.getTel()).willReturn("987");
		given(registrationForm2.getAddress()).willReturn("Rainbow Road");

		editForm = mock(CustomerEditForm.class);
		given(editForm.getFirstname()).willReturn("Gaynor");
		given(editForm.getLastname()).willReturn("Dindorf");
		given(editForm.getAddress()).willReturn("inspiration's dead");
		given(editForm.getTel()).willReturn("1256");
		given(editForm.getReturnDiningSet()).willReturn(0L);

	}

	@Test
	public void createCustomer(){
		Customer returnedCustomer = management.createCustomer(registrationForm2);
		assertEquals(registrationForm2.getFirstname(), returnedCustomer.getFirstname());
		assertEquals(registrationForm2.getLastname(), returnedCustomer.getLastname());
		assertEquals(registrationForm2.getTel(), returnedCustomer.getTel());
		assertEquals(registrationForm2.getAddress(), registrationForm2.getAddress());
	}

	@Test
	public void rejectsUnauthenticatedAccessToController() {
		assertThrows(AuthenticationException.class,
				() -> {
						controller.customers(new ExtendedModelMap());
				});
	}

	@Test
	@WithMockUser(authorities = "ADMIN")
	public void findByTelPos(){
		customers.save(customer);
		Optional<Customer> result = management.findByTel(customer.getTel());
		if(result.isPresent()){
			assertEquals(result.get().getTel(), customer.getTel()); //zeigt in der konsole verschiedene ids an...
		}else{
			fail("no customer with that number");
		}
	}

	@Test
	@WithMockUser(authorities = "ADMIN")
	public void findByTelNeg(){
		Optional<Customer> result = management.findByTel("nichDa");
		assertFalse(result.isPresent());
	}

	@Test
	@WithMockUser(authorities = "ADMIN")
	public void findCustomerPos(){
		Customer result = management.findCustomer(customer.getId());
		assertEquals(result.getId(),customer.getId());
	}

	@Test
	@WithMockUser(authorities = "ADMIN")
	public void findCustomerNeg() {
		assertThrows(NoSuchElementException.class,
				()-> {
					management.findCustomer(0);
				});
	}

	@Test
	public void findAll() {
		Streamable<Customer> all = management.findAll();
		assertEquals(2, all.get().count());
		assertTrue(all.get().allMatch(customerL -> customerL.getId()==customer.getId() || customerL.getId()==customer2.getId()));
	}

	@Test
	void findAllEmpty(){
		customers.deleteAll();
		assertEquals(0, management.findAll().get().count());
	}

	@Test
	void editCustomer(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		management.editCustomer(customer, editForm, redirectAttributes);
		assertEquals(customer.getFirstname(), editForm.getFirstname());
		assertEquals(customer.getLastname(), editForm.getLastname());
		assertEquals(customer.getCountDiningSets(), editForm.getReturnDiningSet());
		assertEquals(customer.getAddress(), editForm.getAddress());
	}

	@Test
	public void newTan(){
		Customer customer = management.createCustomer(registrationForm);
		management.newTan(customer);
		assertNotEquals("INITIAL", customer.getTan());
	}

	@Test
	@WithMockUser(roles = "ORDERMANAGER")
	void addNoDiningSets(){
		management.addDiningSets(0, customer);
		assertEquals(customer.getCountDiningSets(), 0);
	}

	@Test
	void addTodayDiningSets(){

		management.addDiningSets(5, customer2);
		management.addDiningSets(3, customer2);
		assertEquals(customer2.getCountDiningSets(), 8);
	}

	@Test
	void addDiningSetsNewEntry(){
		management.addDiningSets(5, customer);
		businessTime.forward(Duration.ofDays(5));
		management.addDiningSets(10, customer);
		assertEquals(customer.getCountDiningSets(), 15);
	}

	@Test
	void removeNoDiningSets(){

		AtomicBoolean da = new AtomicBoolean(false);
		accountancy.findAll().get().forEach(accountancyEntry ->{
			if(accountancyEntry.getDescription().equals("returned DiningSets from " + customer2.getFullName())){
				System.out.println(accountancyEntry);
				da.getAndSet(true);
			}
		});
		assertFalse(da.get());

		management.addDiningSets(10, customer);

		accountancy.findAll().get().forEach(accountancyEntry ->{
			System.out.println(accountancyEntry);
			if(accountancyEntry.getDescription().equals("returned DiningSets from " + customer.getFullName())){
				da.getAndSet(true);
			}
		});
		assertTrue(da.get());

		management.removeDiningSets(0, customer);

		assertEquals(customer.getCountDiningSets(), 10);
	}

	@Test
	void deleteToOld(){
		management.addDiningSets(10, customer);
		businessTime.forward(Duration.ofDays(40));
		management.removeDiningSets(1, customer);
		assertEquals(customer.getCountDiningSets(), 0);
	}

	@Test
	void removeAllDiningSetsSuccessfull(){
		management.addDiningSets(1, customer);
		businessTime.forward(Duration.ofDays(-5));
		management.addDiningSets(10, customer);
		businessTime.forward(Duration.ofDays(-5));
		management.addDiningSets(9, customer);

		assertEquals(customer.getCountDiningSets(), 20);

		management.removeDiningSets(9, customer);
		assertEquals(customer.getCountDiningSets(), 11);
		assertEquals(customer.getDiningSets().size(), 2);
		management.removeDiningSets(11, customer);
		assertEquals(customer.getCountDiningSets(), 0);
		assertEquals(customer.getDiningSets().size(), 0);
	}

	@Test
	void verifiedCustomerNull(){
		assertNull(management.getVerifiedCustomer());
	}

	@Test
	void verifiedCustomer(){
		management.setVerifiedCustomer(customer);
		assertEquals(management.getVerifiedCustomer(), customer);
	}
}
