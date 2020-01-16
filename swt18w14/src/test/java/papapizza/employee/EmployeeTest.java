package papapizza.employee;

import org.junit.jupiter.api.Test;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.*;


@SpringBootTest
public class EmployeeTest {

	@Autowired
	UserAccountManager userAccountManager;

	@Test
	public void constructor(){
		UserAccount uaA = userAccountManager.create("Jane", "password", Role.of("ROLE_ADMIN"));
		Employee admin = new Employee("123", "address", uaA);

		assertEquals("123", admin.getNumber());
		assertEquals("address", admin.getAddress());
		assertEquals(uaA, admin.getUseraccount());

		admin.setAddress("new address");
		assertEquals("new address", admin.getAddress());

		admin.setNumber("456");
		assertEquals("456", admin.getNumber());
	}

	@Test
	public void roles(){

		UserAccount uaO = userAccountManager.create("John", "password", Role.of("ROLE_ORDERMANAGER"));
		Employee orderManager = new Employee("123", "", uaO);

		assertEquals("Bestellmanager", orderManager.getRole());
		assertTrue(orderManager.roleOrdermanager());

		UserAccount uaD = userAccountManager.create("Jack", "password", Role.of("ROLE_DELIVERER"));
		Employee deliverer = new Employee("123", "", uaD);

		assertEquals("Lieferant", deliverer.getRole());
		assertTrue(deliverer.roleDeliverer());


		UserAccount uaB = userAccountManager.create("Joe", "password", Role.of("ROLE_BAKER"));
		Employee baker = new Employee("123", "", uaB);

		assertEquals("Bäcker", baker.getRole());
		assertTrue(baker.roleBaker());
	}

}
