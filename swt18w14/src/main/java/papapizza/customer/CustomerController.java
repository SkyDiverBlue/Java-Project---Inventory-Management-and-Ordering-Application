package papapizza.customer;

import javax.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;


@Controller
public class CustomerController {

	private final CustomerManagement customerManagement;

	CustomerController(CustomerManagement customerManagement) {

		Assert.notNull(customerManagement, "CustomerManagement must not be null!");

		this.customerManagement = customerManagement;
	}

	//Kunden anlegen

	@GetMapping("/registerCustomer")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_ORDERMANAGER')")
	String register(Model model, CustomerRegistrationForm form) {
		model.addAttribute("form", form);
		return "registerCustomer";
	}

	@PostMapping("/registerCustomer")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_ORDERMANAGER')")
	String registerNew(@Valid CustomerRegistrationForm form, Errors result, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {

			redirectAttributes.addFlashAttribute("registered", false);
			return "registerCustomer";
		}

		if(customerManagement.findByTel(form.getTel()).isPresent()){

			redirectAttributes.addFlashAttribute("telExists", true);
			return "redirect:/registerCustomer";
		}

		redirectAttributes.addFlashAttribute("registered", true);
		customerManagement.createCustomer(form);

		return "redirect:/customers";
	}


	//Kundendaten anzeigen

	@GetMapping("/customers")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_ORDERMANAGER')")
	String customers(Model model) {

		model.addAttribute("customerList", customerManagement.findAll());

		return "customers";
	}



	//Kunden bearbeiten

	@GetMapping("/editCustomer/{id}")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_ORDERMANAGER')")
	String edit(@PathVariable long id, Model model, CustomerRegistrationForm form ) {

		model.addAttribute("customer", customerManagement.findCustomer(id));
		model.addAttribute("form", form);

		return "editCustomer";
	}

	@PostMapping("/editCustomer/{id}")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_ORDERMANAGER')")
	String editcustomer(@Valid CustomerEditForm form, Errors result, @PathVariable long id,
						RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {

			redirectAttributes.addFlashAttribute("edited", false);
			return "redirect:/editCustomer/{id}";
		}

		Customer customer = customerManagement.findCustomer(id);

		redirectAttributes = customerManagement.editCustomer(customer, form, redirectAttributes);
		redirectAttributes.addFlashAttribute("edited", true);
		redirectAttributes.addFlashAttribute("return", form.getReturnDiningSet());

		if(redirectAttributes.getFlashAttributes().containsKey("telExists")){
			return "redirect:/editCustomer/{id}";
		}

		return "redirect:/customers";
	}

	//Kunden löschen

	@GetMapping("/deleteCustomer/{id}")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_ORDERMANAGER')")
	String deletecustomer(@PathVariable long id, RedirectAttributes redirectAttributes) {

		Customer customer = customerManagement.findCustomer(id);

		customerManagement.removeCustomer(customer);

		redirectAttributes.addFlashAttribute("deleted", true);

		return "redirect:/customers";
	}

	//Kunden für Bestellung mittels Telefonnummer und TAN verifizieren

	@GetMapping("/verifyCustomer")
	@PreAuthorize("hasRole('ROLE_ORDERMANAGER')")
	String verifyCustomer(Model model, CustomerVerifyingForm form) {
		model.addAttribute("form", form);

		return "verifyCustomer";
	}

	@PostMapping("/verifyCustomer")
	@PreAuthorize("hasRole('ROLE_ORDERMANAGER')")
	String verifyCustomer(Model model, @Valid CustomerVerifyingForm form, RedirectAttributes redirectAttributes) {

		if (customerManagement.findByTel(form.getTel()).isPresent()) {

			Customer customer = customerManagement.findByTel(form.getTel()).get();

			if (customer.getTan().equals(form.getTan())) {
				model.addAttribute("customer", customer);
				customerManagement.setVerifiedCustomer(customer);
				return "redirect:/orderScreen/";
			}
			redirectAttributes.addFlashAttribute("message", 2);
			return "redirect:/verifyCustomer";
		}
		redirectAttributes.addFlashAttribute("message", 1);
		return "redirect:/verifyCustomer";
	}

	@GetMapping("/searchc")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_ORDERMANAGER')")
	String search(@RequestParam("search") String search, Model model) {

		ArrayList<Customer> customers = new ArrayList<>();
		boolean res=false;

		if(search.equals("")) {
			model.addAttribute("customerList", customerManagement.findAll());
		} else{
			customerManagement.findAll().forEach(customer -> {
				if(customer.getFullName().toLowerCase().contains(search.toLowerCase())){
					customers.add(customer);
				}
			});
			res=customers.isEmpty();
			if(res) {
				model.addAttribute("customerList",customerManagement.findAll());
			} else {
				model.addAttribute("customerList",customers);
			}
		}
		model.addAttribute("noresult",res);

		return "customers";
	}

}