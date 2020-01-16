package papapizza.employee;

import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * The Employee Class represents the staff of PapaPizza.
 * It is used to save information about each employee such as their name, address, phone number and role.
 * The role represents the employees field of activity which can be one of the following:
 * - baker, deliverer or ordermanager
 * Every employee has one useraccount which contains their login details (username, password and role),
 * such as their firstname and lastname.
 * The employees can be saved in the EmployeeRepository.
 * @author Toni Tanneberger
 */

@Entity
public class Employee {

	@OneToOne
	private UserAccount useraccount;

	public @Id @GeneratedValue long id;
	private String address;
	private String number;

	@SuppressWarnings("unused")
	private Employee() {}

	public Employee(String number, String address, UserAccount useraccount) {
		this.useraccount = useraccount;
		this.number = number;
		this.address = address;
	}

	/**
	 * Returns the employees useraccount.
	 * @return will never we null.
	 */
	public UserAccount getUseraccount() {
		return useraccount;
	}

	/**
	 * Returns the unique identifier of the employee.
	 * @return will never be null.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Returns the employees phone number.
	 * @return will never be null.
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Sets the employees phone number to a new one.
	 * @param number - new phone number of the employee.
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * Returns the address of the employee.
	 * @return will never be null.
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Sets the employees address to a new one.
	 * @param address - new address for the employee.
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * Returns True if the employee has the role baker.
	 * @return will never be null.
	 */
	public Boolean roleBaker()	{
		return getUseraccount().hasRole(Role.of("ROLE_BAKER"));
	}

	/**
	 * Returns True if the employee has the role ordermanager.
	 * @return will never be null.
	 */
	public Boolean roleOrdermanager()	{
		return getUseraccount().hasRole(Role.of("ROLE_ORDERMANAGER"));
	}

	/**
	 * Returns True if the employee has the role deliverer.
	 * @return will never be null.
	 */
	public Boolean roleDeliverer()	{
		return getUseraccount().hasRole(Role.of("ROLE_DELIVERER"));
	}

	/**
	 * Returns the German word for the role of the employee.
	 * @return will never be null.
	 */
	public String getRole() {
		if(getUseraccount().hasRole(Role.of("ROLE_BAKER"))) {
			return "Bäcker";
		} else if(getUseraccount().hasRole(Role.of("ROLE_ORDERMANAGER"))) {
			return "Bestellmanager";
		} else {
			return "Lieferant";
		}
	}
}
