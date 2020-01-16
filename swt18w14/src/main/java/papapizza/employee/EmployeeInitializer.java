package papapizza.employee;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Initializes an admin account and a few other employees.
 */
@Component
@Order(10)
public class EmployeeInitializer implements DataInitializer {

	private final EmployeeRepository employees;
	private final UserAccountManager userAccountManager;

	EmployeeInitializer(UserAccountManager userAccountManager, EmployeeRepository employees) {

		Assert.notNull(employees, "emplyoeeRepository must not be null!");
		Assert.notNull(userAccountManager, "UserAccountManager must not be null!");

		this.userAccountManager = userAccountManager;
		this.employees=employees;
	}

	@Override

	public void initialize() {

		//hardcode bossaccount

		if (employees.findAll().iterator().hasNext()) {
			return;
		}

		UserAccount bossAccount = userAccountManager.create("boss", "123", Role.of("ROLE_ADMIN"));
		userAccountManager.save(bossAccount);


		UserAccount ua1 = userAccountManager.create("b", "123", Role.of("ROLE_BAKER"));
		UserAccount ua2 = userAccountManager.create("o", "123", Role.of("ROLE_ORDERMANAGER"));
		UserAccount ua3 = userAccountManager.create("d", "123", Role.of("ROLE_DELIVERER"));
		UserAccount ua4 = userAccountManager.create("b2", "123", Role.of("ROLE_BAKER"));
 		ua1.setFirstname("Hans");
 		ua1.setLastname("Günter");
 		ua2.setFirstname("Hans2");
 		ua2.setLastname("Wurst");
 		ua3.setLastname("Luxenburg");
 		ua3.setFirstname("Rosa");
 		ua4.setFirstname("Torsten");
 		ua4.setLastname("Nya~");

		Employee e1 = new Employee("1111","Da", ua1);
		Employee e2 = new Employee("11112","Da2", ua2);
		Employee e3 = new Employee("33", "Rosa_Luxenburg_Alee", ua3);
		Employee e4 = new Employee("68", "Im dunklen Wald", ua4);

		if (employees.count() == 0) {
			employees.save(e1);
			employees.save(e2);
			employees.save(e3);
			employees.save(e4);
		}
	}
}
