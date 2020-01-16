package papapizza.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.validation.Errors;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class EmployeeControllerTest {

	@Autowired
	private EmployeeManagement employeeManagement;

	@Autowired
	private EmployeeController employeeController;


	private Model model = new ExtendedModelMap();
	private Errors hasErrors;
	private Errors noErrors;
	private EmployeeRegistrationForm form;
	private EmployeeRegistrationForm form2;
	private EmployeeEditForm editform;

	@BeforeEach
	public void setUp(){
		MockitoAnnotations.initMocks(this);

		hasErrors = mock(Errors.class);
		given(hasErrors.hasErrors()).willReturn(true);

		noErrors = mock(Errors.class);
		given(noErrors.hasErrors()).willReturn(false);

		form = mock(EmployeeRegistrationForm.class);
		given(form.getFirstname()).willReturn("Jane");
		given(form.getLastname()).willReturn("Doe");
		given(form.getType()).willReturn("b");
		given(form.getAddress()).willReturn("Rainbow Road");
		given(form.getNumber()).willReturn("123");
		given(form.getUsername()).willReturn("JD");
		given(form.getPassword()).willReturn("unsafe");

		form2 = mock(EmployeeRegistrationForm.class);
		given(form2.getFirstname()).willReturn("Kevin");
		given(form2.getLastname()).willReturn("Tracy");
		given(form2.getType()).willReturn("b");
		given(form2.getAddress()).willReturn("Rainbow Road");
		given(form2.getNumber()).willReturn("123");
		given(form2.getUsername()).willReturn("KT");
		given(form2.getPassword()).willReturn("unsafe");

		editform = mock(EmployeeEditForm.class);
		given(editform.getFirstname()).willReturn("Joane");
		given(editform.getLastname()).willReturn("Doe");
		given(editform.getType()).willReturn("d"); //role deliverer
		given(editform.getAddress()).willReturn("Star Road");
		given(editform.getNumber()).willReturn("234");

	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void registerNewErrors(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = employeeController.registerNew(form, hasErrors, redirectAttributes);

		assertTrue(redirectAttributes.getFlashAttributes().get("registered").equals(false));
		assertThat(returnedView).isEqualTo("redirect:/employees");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void registerNewNoErrorsUAExists(){ //register new employee using already existing UA userame
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = employeeController.registerNew(form, noErrors, redirectAttributes);

		assertEquals(false, redirectAttributes.getFlashAttributes().get("registered"));
		assertEquals(true, redirectAttributes.getFlashAttributes().get("nameExists"));
		assertThat(returnedView).isEqualTo("redirect:/employees");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void registerNewNoErrors(){ //register new employee with unused UA userame
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = employeeController.registerNew(form2, noErrors, redirectAttributes);

		assertEquals(true, redirectAttributes.getFlashAttributes().get("registered"));
		assertThat(returnedView).isEqualTo("redirect:/employees");
	}


	@Test
	@WithMockUser(roles = "ADMIN")
	public void viewEmployees(){
		String returnedView = employeeController.employees(model);

		assertThat(returnedView).isEqualTo("employees");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void editNewErrors(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		long id = employeeManagement.findAllEmployees().get().findFirst().get().id;
		String returnedView = employeeController.editnew(id, editform, hasErrors, redirectAttributes);

		assertTrue(redirectAttributes.getFlashAttributes().get("edit").equals(false));
		assertThat(returnedView).isEqualTo("redirect:/employees");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void editNewNoErrors(){

		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		long id = employeeManagement.findAllEmployees().get().findFirst().get().id;
		String returnedView = employeeController.editnew(id, editform, noErrors, redirectAttributes);

		assertEquals(true, redirectAttributes.getFlashAttributes().get("edit"));
		assertThat(returnedView).isEqualTo("redirect:/employees");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void edit(){
		String returnedView = employeeController.edit(employeeManagement.findAllEmployees().get().findFirst().get().getId(),
				model, null);

		assertThat(returnedView).isEqualTo("editEmployee");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void register(){
		String returnedView = employeeController.register(model, null);

		assertThat(returnedView).isEqualTo("registerEmployee");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void delete(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = employeeController.delete(employeeManagement.findAllEmployees().get().findFirst().get().getId(), redirectAttributes);

		assertEquals(true, redirectAttributes.getFlashAttributes().get("deleted"));
		assertThat(returnedView).isEqualTo("redirect:/employees");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void searchEmptyString(){
		Model model = new ExtendedModelMap();
		String returnedView = employeeController.search("", model);

		assertEquals(false, ((ExtendedModelMap) model).get("noresult"));
		assertTrue(model.containsAttribute("noresult"));
		assertThat(returnedView).isEqualTo("employees");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void searchNoResult(){
		Model model = new ExtendedModelMap();
		String returnedView = employeeController.search("impossible search query", model);

		assertEquals(true, ((ExtendedModelMap) model).get("noresult"));
		assertThat(returnedView).isEqualTo("employees");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void search(){
		Model model = new ExtendedModelMap();
		String returnedView = employeeController.search("J", model);

		assertEquals(false, ((ExtendedModelMap) model).get("noresult"));
		assertThat(returnedView).isEqualTo("employees");
	}

	@Test
	public void getFullName(){
		Employee employee = employeeManagement.createEmployee(form);
		String fullName = employeeController.getFullName(employee);

		assertEquals("Jane Doe", fullName);
	}
}
