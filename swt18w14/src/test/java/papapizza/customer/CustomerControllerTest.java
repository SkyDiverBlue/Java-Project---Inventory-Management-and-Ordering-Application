package papapizza.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


@SpringBootTest
@Transactional
public class CustomerControllerTest {

	@Autowired
	private CustomerManagement customerManagement;

	@Autowired
	private CustomerController customerController;

	@Autowired
	private CustomerRepository customerRepository;

	private Model model = new ExtendedModelMap();
	private Customer customer;
	private CustomerRegistrationForm customerRegistrationForm;
	private CustomerRegistrationForm customerRegistrationFormExists;
	private CustomerEditForm customerEditForm;
	private CustomerVerifyingForm customerVerifyingForm;
	private CustomerVerifyingForm customerVerifyingFormWrongTAN;
	private CustomerVerifyingForm customerVerifyingFormWrongNumber;
	private Errors hasErrors;
	private Errors noErrors;


	@BeforeEach
	public void setUp(){
		customerRepository.deleteAll(); //to make sure that the repo is empty

		customer = new Customer("John", "Smith", "0491");
		customer.setAddress("holiday soon");
		customerRepository.save(customer);

		customerRegistrationForm = mock(CustomerRegistrationForm.class);
		given(customerRegistrationForm.getFirstname()).willReturn("Tom");
		given(customerRegistrationForm.getLastname()).willReturn("Kravitz");
		given(customerRegistrationForm.getAddress()).willReturn("manhole");
		given(customerRegistrationForm.getTel()).willReturn("8567");

		customerRegistrationFormExists = mock(CustomerRegistrationForm.class);
		given(customerRegistrationFormExists.getFirstname()).willReturn("Tom");
		given(customerRegistrationFormExists.getLastname()).willReturn("Kravitz");
		given(customerRegistrationFormExists.getAddress()).willReturn("manhole");
		given(customerRegistrationFormExists.getTel()).willReturn("0491"); //number already attributed

		customerEditForm = mock(CustomerEditForm.class);
		given(customerEditForm.getFirstname()).willReturn("Balthasar");
		given(customerEditForm.getLastname()).willReturn("Melchior");
		given(customerEditForm.getAddress()).willReturn("manger");
		given(customerEditForm.getTel()).willReturn("1238924");
		given(customerEditForm.getReturnDiningSet()).willReturn(8L);

		customerVerifyingForm = mock(CustomerVerifyingForm.class);
		given(customerVerifyingForm.getTan()).willReturn("INITIAL");
		given(customerVerifyingForm.getTel()).willReturn("0491");

		customerVerifyingFormWrongTAN = mock(CustomerVerifyingForm.class);
		given(customerVerifyingFormWrongTAN.getTan()).willReturn("WRONG TAN");
		given(customerVerifyingFormWrongTAN.getTel()).willReturn("0491");

		customerVerifyingFormWrongNumber = mock(CustomerVerifyingForm.class);
		given(customerVerifyingFormWrongNumber.getTan()).willReturn("INITIAL");
		given(customerVerifyingFormWrongNumber.getTel()).willReturn("WRONG NUMBER");

		hasErrors = mock(Errors.class);
		given(hasErrors.hasErrors()).willReturn(true);

		noErrors = mock(Errors.class);
		given(noErrors.hasErrors()).willReturn(false);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void customers(){
		String returnedView = customerController.customers(model);
		assertThat(returnedView).isEqualTo("customers");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void register(){
		String returnedView = customerController.register(model, null);
		assertThat(returnedView).isEqualTo("registerCustomer");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void registerNewErrors(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = customerController.registerNew(customerRegistrationForm, hasErrors, redirectAttributes);

		assertEquals(false, redirectAttributes.getFlashAttributes().get("registered"));
		assertThat(returnedView).isEqualTo("registerCustomer");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void registerNewAlreadyExitsts(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = customerController.registerNew(customerRegistrationFormExists, noErrors, redirectAttributes);

		assertEquals(true, redirectAttributes.getFlashAttributes().get("telExists"));
		assertThat(returnedView).isEqualTo("redirect:/registerCustomer");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void registerNewSuccessful(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = customerController.registerNew(customerRegistrationForm, noErrors, redirectAttributes);

		assertEquals(true, redirectAttributes.getFlashAttributes().get("registered"));
		assertTrue(customerManagement.findByTel(customerRegistrationForm.getTel()).isPresent());
		assertThat(returnedView).isEqualTo("redirect:/customers");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void edit(){
		String returnedView = customerController.edit(customerManagement.findAll().get().findFirst().get().getId(),
				model, null);

		assertThat(returnedView).isEqualTo("editCustomer");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void editCustomerErrors(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = customerController.editcustomer(customerEditForm, hasErrors, customer.getId(),
				redirectAttributes);

		assertEquals(false, redirectAttributes.getFlashAttributes().get("edited"));
		assertThat(returnedView).isEqualTo("redirect:/editCustomer/{id}");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void editCustomerSuccessful(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = customerController.editcustomer(customerEditForm, noErrors, customer.getId(),
				redirectAttributes);

		assertEquals(true, redirectAttributes.getFlashAttributes().get("edited"));
		assertEquals(customerEditForm.getFirstname(), customer.getFirstname());
		assertEquals(customerEditForm.getLastname(), customer.getLastname());
		assertEquals(customerEditForm.getAddress(), customerEditForm.getAddress());
		assertEquals(customerEditForm.getTel(), customerEditForm.getTel());
		assertThat(returnedView).isEqualTo("redirect:/customers");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void deleteCustomer(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		Customer customer = customerManagement.findAll().get().findFirst().get();
		String returnedView = customerController.deletecustomer(customer.getId(),
				redirectAttributes);

		assertThrows(NoSuchElementException.class, ()-> {
			customerManagement.findCustomer(customer.getId());
				}

		);
		assertEquals(true, redirectAttributes.getFlashAttributes().get("deleted"));
		assertThat(returnedView).isEqualTo("redirect:/customers");
	}

	@Test
	@WithMockUser(roles = "ORDERMANAGER")
	public void verifyCustomer(){
		String returnedView = customerController.verifyCustomer(model, null);

		assertThat(returnedView).isEqualTo("verifyCustomer");
	}

	@Test
	@WithMockUser(roles = "ORDERMANAGER")
	public void verifyCustomerSuccessful(){
		Model model = new ExtendedModelMap();
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

		Customer customer = customerManagement.findByTel(customerVerifyingForm.getTel()).get();
		customer.setTan("INITIAL"); //To make sure the TAN really is on INITAL

		String returnedView = customerController.verifyCustomer(model, customerVerifyingForm, redirectAttributes);
		assertTrue(model.containsAttribute("customer"));
		assertThat(returnedView).isEqualTo("redirect:/orderScreen/");
	}

	@Test
	@WithMockUser(roles = "ORDERMANAGER")
	public void verifyCustomerWrongTAN(){
		Model model = new ExtendedModelMap();
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

		Customer customer = customerManagement.findByTel(customerVerifyingForm.getTel()).get();
		customer.setTan("RIGHT TAN"); //To make sure the TAN really is on RIGHT TAN

		String returnedView = customerController.verifyCustomer(model, customerVerifyingFormWrongTAN, redirectAttributes);

		assertEquals(2, redirectAttributes.getFlashAttributes().get("message"));
		assertThat(returnedView).isEqualTo("redirect:/verifyCustomer");
	}

	@Test
	@WithMockUser(roles = "ORDERMANAGER")
	public void verifyCustomerWrongNumber(){
		Model model = new ExtendedModelMap();
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

		String returnedView = customerController.verifyCustomer(model, customerVerifyingFormWrongNumber, redirectAttributes);

		assertEquals(1, redirectAttributes.getFlashAttributes().get("message"));
		assertThat(returnedView).isEqualTo("redirect:/verifyCustomer");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void searchEmptyString(){
		Model model = new ExtendedModelMap();
		String returnedView = customerController.search("", model);

		assertEquals(false, ((ExtendedModelMap) model).get("noresult"));
		assertTrue(model.containsAttribute("customerList"));
		assertThat(returnedView).isEqualTo("customers");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void searchNoResult(){
		Model model = new ExtendedModelMap();
		String returnedView = customerController.search("impossible search no result", model);

		assertEquals(true, ((ExtendedModelMap) model).get("noresult"));
		assertTrue(model.containsAttribute("customerList"));
		assertThat(returnedView).isEqualTo("customers");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void searchResults(){
		Model model = new ExtendedModelMap();
		String returnedView = customerController.search("j", model);

		assertEquals(false, ((ExtendedModelMap) model).get("noresult"));
		assertTrue(model.containsAttribute("customerList"));
		assertThat(returnedView).isEqualTo("customers");

	}

}

