package papapizza.bill;



import org.salespointframework.accountancy.Accountancy;
import org.salespointframework.accountancy.AccountancyEntry;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import papapizza.customer.Customer;
import papapizza.customer.CustomerManagement;
import papapizza.order.CustomerOrder;

import java.util.ArrayList;


/**
 * The Controller manages all the finished Orders
 * @author Felix Fischer
 */
@Controller
@PreAuthorize("hasAnyRole('ROLE_DELIVERER', 'ROLE_ADMIN', 'ROLE_ORDERMANAGER')")				//change to Deliverer
public class BillController{

	private final Accountancy accountancy;
	private final CustomerManagement customerManagement;
	private final OrderRepo orderRepo;


	/**
	 * @param accountancy
	 * @param customerManagement
	 */
	public BillController(Accountancy accountancy, CustomerManagement customerManagement, OrderRepo orderRepo){
		this.accountancy = accountancy;
		this.customerManagement = customerManagement;
		this.orderRepo = orderRepo;
	}



	/**
	*Creates by searching for the billId a new Website who prints out all important information about this bill
	 * @param id - represents the UUID of the specific bill
	 * @param model
	 * @return form
	 * @return redirect
	 */
	@RequestMapping(value="/printBill", method=RequestMethod.POST)
	public String showTheBill(@RequestParam String id, Model model){
		CustomerOrder customerOrder = null;
		for (CustomerOrder customOrder: orderRepo.findAll()){
			if (customOrder.getId().toString().equals(id)){
				customerOrder = customOrder;

			}
		}

		if (customerOrder != null){
			if (!customerOrder.getDeliverableState()) {
				handleAccountingEntrys(customerOrder);
			}
			String information = " ";
			if (!customerOrder.getDeliverableState() && customerOrder.isCoupon()){
				information= (" Sie bekommen einen Rabatt von 20%, da " +
						"Sie die Bestellung vor Ort abhohlen und einen Coupon eingelöst haben");
			}
			else if (!customerOrder.isCoupon() && !customerOrder.getDeliverableState()){
				information = ("Sie bekommen, da sie die Bestellung selbst abholen" +
						" einen Rabatt von 10 %");
			}
			else if(customerOrder.isCoupon() && customerOrder.getDeliverableState()){
				information = ("Sie erhalten 10% Rabatt für die Einlösung des Coupons");
			}


			model.addAttribute("stayingPolite", "Vielen Dank, dass sie sich für PapaPizza entschieden haben" );
			model.addAttribute("tan", customerOrder.getCustomer().getTan());
			model.addAttribute("products", customerOrder.getOrderLines().get().toArray());
			model.addAttribute("totalPrice", customerOrder.getTotalPrice());
			model.addAttribute("informationAboutThePrice", information);
			if (customerOrder.isDiscounted()) {
				model.addAttribute("discounted" , customerOrder.isDiscounted());
				model.addAttribute("discount", customerOrder.getDiscount());
			} else {
				model.addAttribute("discounted", customerOrder.isDiscounted());
			}
		}
		return  "printBill";
	}


	/**
	*this function simply adds the price to the Accounting
	 * @param id - represents the UUID of the specific bill
	 * @return form
	 * @return redirect
	 */
	@RequestMapping(value="addNewPizzaToAccountancy", method=RequestMethod.POST)
	String addNewAccountingEntry(@RequestParam("id") String id){
		CustomerOrder customerOrder = null;
		for (CustomerOrder customOrder: orderRepo.findAll()){
			if (customOrder.getId().toString().equals(id)){
				customerOrder = customOrder;
			}
		}

		handleAccountingEntrys(customerOrder);
		return "redirect:/deliverableProducts";
	}

	/**
	* If a customer chooses to return hin dining-Sets, this functions try's to find the customer, reduces
	 * the amount of dining
	* Sets related to the Customer and add a new Accountancy-Entry by multiplying the amount of returned
	 * Dining-Sets by -15
	* @param id - represents the UUID of the specific bill
	* @param amount - amount of dining Sets
	* @return form
	* @return redirect
	 */
	@PostMapping("returnDiningSetsO")
	@PreAuthorize("hasRole('ROLE_ORDERMANAGER')")
	String returnDiningSetsO(@RequestParam("id") String id, @RequestParam("value") int amount, RedirectAttributes redirectAttributes){

		redirectAttributes = returnSets(amount, id, redirectAttributes);
		redirectAttributes.addFlashAttribute("ordermanager", true);

		return "redirect:/productsReadyForPickUp";
	}
	@PostMapping("returnDiningSetsD")
	@PreAuthorize("hasRole('ROLE_DELIVERER')")
	String returnDiningSetsD(@RequestParam("id") String id, @RequestParam("value") int amount, RedirectAttributes redirectAttributes){

		redirectAttributes = returnSets(amount, id, redirectAttributes);
		redirectAttributes.addFlashAttribute("deliverer", true);

		return "redirect:/deliverableProducts";
	}

	/**
	* filters all finished Orders by their deliverable state and returns just the ones, who needs to be delivered
	*  @param model
	 * @return form
	 * @return redirect
	 */
	@PreAuthorize("hasRole('ROLE_DELIVERER')")
	@RequestMapping(value = "deliverableProducts", method = RequestMethod.GET)
	String showAllOrdersReadyForDelivery(Model model){
		ArrayList<CustomerOrder> temp = new ArrayList<>();
		for (CustomerOrder customerOrder: orderRepo.findAll()) {
			if (customerOrder.getCompletedState()) {
				if (customerOrder.getDeliverableState()) {
					temp.add(customerOrder);
				}
			}
		}
		model.addAttribute("bills", temp);						//baue filter ein
		return "deliverableProducts";
	}

	/**
	 * filters all finished Orders by their deliverable state and returns just the ones, who are going to get picked up
	 * @param model
	 * @return form
	 * @return redirect
	 */
	@PreAuthorize("hasRole('ROLE_ORDERMANAGER')")
	@RequestMapping(value = "productsReadyForPickUp", method = RequestMethod.GET)
	String showAllOrdersReadyForPickUp(Model model){
		ArrayList<CustomerOrder> temp = new ArrayList<>();
		for (CustomerOrder customerOrder: orderRepo.findAll()) {
			if (customerOrder.getCompletedState()) {
				if (!customerOrder.getDeliverableState()) {
					temp.add(customerOrder);
				}
			}
		}

		model.addAttribute("bills", temp);
		return "productsReadyForPickUp";
	}


	/**
	 * shows all completed Orders
	 * @parm model
	 * @return form
	 * @return redirect
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/completedOrders")
	String complete(Model model){
		ArrayList<CustomerOrder> temp = new ArrayList<>();
		for (CustomerOrder customerOrder: orderRepo.findAll()) {
			if (customerOrder.getCompletedState()){
				temp.add(customerOrder);
			}
		}
		model.addAttribute("bills", temp);
		model.addAttribute("completedOrders", orderRepo.findAll());

		return "completedOrders";
	}



	/**
	* manages the Accountancy-Entry
	* and deletes the Order from the List
	* @param customerOrder - takes a CustomerOrder-Object
	 * @return void
	 */
	private void handleAccountingEntrys(CustomerOrder customerOrder){
		if (customerOrder != null) {
			customerOrder.isPaid();
			accountancy.add(new AccountancyEntry(customerOrder.getTotalPrice(),
					"new Order for " + customerOrder.getCustomer().getFullName() ));
			orderRepo.delete(customerOrder);
			System.out.println(orderRepo.count());
		} else {
			System.out.println("Something went wrong in Accountancy");
		}
	}

	private RedirectAttributes returnSets(int amount, String id, RedirectAttributes redirectAttributes){

		if(amount<=0){
			redirectAttributes.addFlashAttribute("notValid", true);
			return redirectAttributes;
		}

		CustomerOrder customerOrder = null;
		for (CustomerOrder customOrder: orderRepo.findAll()){
			if (customOrder.getId().toString().equals(id)){
				customerOrder = customOrder;
			}
		}
		Customer customer = customerOrder.getCustomer();

		long unreturned = customerManagement.removeDiningSets(amount, customer);

		if(amount-unreturned<=0){
			redirectAttributes.addFlashAttribute("noReturn", true);
			return redirectAttributes;
		}

		redirectAttributes.addFlashAttribute("returned", amount);
		redirectAttributes.addFlashAttribute("unreturned", unreturned);

		return redirectAttributes;
	}
}
