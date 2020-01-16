package papapizza.employee;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.salespointframework.useraccount.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@SpringBootTest
public class EmployeeManagementTest {

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	EmployeeManagement employeeManagement;

	private Employee baker;

	private EmployeeRegistrationForm form;
	private EmployeeEditForm editform;

	@BeforeEach
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		form = mock(EmployeeRegistrationForm.class);
		given(form.getFirstname()).willReturn("Jane");
		given(form.getLastname()).willReturn("Doe");
		given(form.getType()).willReturn("b"); //role baker
		given(form.getAddress()).willReturn("Rainbow Road");
		given(form.getNumber()).willReturn("123");
		given(form.getUsername()).willReturn("FV");
		given(form.getPassword()).willReturn("unsafe");

		editform = mock(EmployeeEditForm.class);
		given(editform.getFirstname()).willReturn("Joane");
		given(editform.getLastname()).willReturn("Doe");
		given(editform.getType()).willReturn("o"); //role ordermanager
		given(editform.getAddress()).willReturn("Star Road");
		given(editform.getNumber()).willReturn("234");
	}


	@Test
	public void createEmployee(){
		EmployeeRegistrationForm form = mock(EmployeeRegistrationForm.class);
		given(form.getFirstname()).willReturn("Jane");
		given(form.getLastname()).willReturn("Doe");
		given(form.getType()).willReturn("b"); //role baker
		given(form.getAddress()).willReturn("Rainbow Road");
		given(form.getNumber()).willReturn("123");
		given(form.getUsername()).willReturn("Unique");
		given(form.getPassword()).willReturn("unsafe");

		baker = employeeManagement.createEmployee(form);
		assertTrue(baker.roleBaker());
		assertEquals(baker.getUseraccount().getFirstname(), form.getFirstname());
		assertEquals(baker.getUseraccount().getLastname(), form.getLastname());
		assertEquals(baker.getAddress(), form.getAddress());
		assertEquals(baker.getNumber(), form.getNumber());
	}

	@Test
	public void editEmployee(){
		baker = employeeManagement.editEmployee(editform, employeeRepository.findAll().iterator().next().id);
		assertTrue(baker.roleOrdermanager());
		assertEquals(baker.getNumber(), editform.getNumber());
		assertEquals(baker.getAddress(), editform.getAddress());
		assertEquals(baker.getUseraccount().getFirstname(), editform.getFirstname());
		assertEquals(baker.getUseraccount().getLastname(), editform.getLastname());
	}

	@Test
	public void countBakers(){
		long bakerAmount = employeeManagement.countBakers();
		employeeManagement.createEmployee(form);
		assertEquals(bakerAmount + 1, employeeManagement.countBakers());
	}


	@Test
	public void setRole(){
		String b = "b";
		Role roleB = Role.of("ROLE_BAKER");
		assertEquals(roleB, employeeManagement.setRole(b));

		String o = "o";
		Role roleO = Role.of("ROLE_ORDERMANAGER");
		assertEquals(roleO, employeeManagement.setRole(o));

		String d = "d";
		Role roleD = Role.of("ROLE_DELIVERER");
		assertEquals(roleD, employeeManagement.setRole(d));
	}

	@Test
	public void delete(){
		long id = employeeRepository.findAll().iterator().next().id;
		employeeManagement.delete(id);
		assertFalse(employeeRepository.existsById(id));
	}


}
