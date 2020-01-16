package papapizza.employee;

import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.transaction.annotation.Transactional;
import java.util.Iterator;

/**
 * The management class for every action involving employees.
 * @author Toni Tanneberger
 */
@Service
@Transactional
public class EmployeeManagement {

	private final EmployeeRepository employees;
	private final UserAccountManager userAccounts;


	EmployeeManagement(EmployeeRepository employees, UserAccountManager userAccounts) {

		Assert.notNull(employees, "employeeRepository must not be null!");
		Assert.notNull(userAccounts, "UserAccountManager must not be null!");

		this.employees=employees;
		this.userAccounts = userAccounts;
	}

	/**
	 * Creates and saves a new employee with the given information from the form.
	 * @param form filled in form which contains data of the employee to be created
	 * @return
	 */
	public Employee createEmployee(EmployeeRegistrationForm form) {

		Assert.notNull(form, "Registration form must not be null!");

		String type = form.getType();
		Role role = this.setRole(type);
		UserAccount useraccount = userAccounts.create(form.getUsername(), form.getPassword(), role);

		useraccount.setFirstname(form.getFirstname());
		useraccount.setLastname(form.getLastname());


		return employees.save(new Employee(form.getNumber(), form.getAddress(), useraccount));
	}

	/**
	 * Edits the information of a given employee using the filled in form.
	 * @param form filled in form containing the new information of the employee
	 * @param id unique identifier of the employee who will be edited
	 * @return
	 */
	public Employee editEmployee(EmployeeEditForm form, long id) {

		Assert.notNull(form, "Edit form must not be null!");
		Employee e = employees.findById(id).get();
		e.setAddress(form.getAddress());
		e.setNumber(form.getNumber());
		e.getUseraccount().setFirstname(form.getFirstname());
		e.getUseraccount().setLastname(form.getLastname());



		String type = form.getType();
		Role role = this.setRole(type);

		if(e.getUseraccount().hasRole(Role.of("ROLE_BAKER"))) {
			e.getUseraccount().remove(Role.of("ROLE_BAKER"));
		} else if(e.getUseraccount().hasRole(Role.of("ROLE_ORDERMANAGER"))) {
			e.getUseraccount().remove(Role.of("ROLE_ORDERMANAGER"));
		} else {
			e.getUseraccount().remove(Role.of("ROLE_DELIVERER"));
		}
		e.getUseraccount().add(role);


		return employees.save(e);
	}

	/**
	 * Returns an employee out of the EmployeeRepository using their id.
	 * @param id unique identifier to find the wanted employee
	 * @return should not be null.
	 */
	public Employee findEmployee(long id){
		return employees.findById(id).get();
	}

	/**
	 * Returns every employee currently in the EmployeeRepository.
	 * @return
	 */
	public Streamable<Employee> findAllEmployees() {

		return Streamable.of(employees.findAll());
	}

	/**
	 * Returns the number of employees with the role baker.
	 */
	public long countBakers() {
		long l=0;
		Iterator<Employee> it = employees.findAll().iterator();
		while(it.hasNext()) {
			if(it.next().roleBaker()) {
				l++;
			}
		}
		return l;
	}

	/**
	 * Returns a role depending on the given type.
	 * @param type represents a role, given by the EmployeeForms
	 * @return will never be null.
	 */
	public Role setRole(String type) {
		Role role;
		if (type.equals("b")) {
			role = Role.of("ROLE_BAKER");
		} else if (type.equals("o")) {
			role = Role.of("ROLE_ORDERMANAGER");
		} else {
			role = Role.of("ROLE_DELIVERER");
		}
		return role;
	}

	/**
	 * Deletes an employee with a specific id.
	 * @param id unique identifier of the employee to be deleted
	 */
	public void delete(long id) {
		Employee e = findEmployee(id);
		e.getUseraccount().setEnabled(false);
		 employees.delete(e);
	}
}
