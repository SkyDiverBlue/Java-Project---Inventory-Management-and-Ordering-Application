package papapizza.customer;

import javax.persistence.*;

import org.salespointframework.useraccount.UserAccount;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  A Customer is supposed to contain every, for the application relevant information,relating to the natural person,
 *  being Customer of the client PapaPizza.
 *  Those information include firstname, lastname, telephone number, address
 *  and an overview of rented dining-sets and their termination date.
 *  <br>This class creates a Customer that can be saved in a database (CustomerRepository).
 * @author Emely Himmstedt
 */

@Entity
public class Customer{

	private @Id @GeneratedValue long id;

	private String tan;

    private String address;

	@Column(length = 10000)
	private ConcurrentHashMap<LocalDate, Long> diningSets;

	private String firstname;

	private String lastname;

	private String tel;


	@SuppressWarnings("unused")
	private Customer() {}

    public Customer(String firstname, String lastname, String tel) {
		this.tel = tel;
        this.tan = "INITIAL";
        this.firstname = firstname;
        this.lastname = lastname;
        diningSets = new ConcurrentHashMap<>();
    }


	/**
	 * Get the unique identifier of this {@link Customer}.
	 *
	 * @return the long id of this <code>Customer</code>
	 */
	public long getId() { return id; }

	/**
	 * Returns the customer's firstname.
	 *
	 * @return will never be {@literal null}.
	 */
    public String getFirstname() { return firstname; }

	public void setFirstname(String firstname) { this.firstname = firstname; }

	/**
	 * Returns the customer's lastname.
	 *
	 * @return will never be {@literal null}.
	 */
	public String getLastname() { return lastname; }

	public void setLastname(String lastname) { this.lastname = lastname; }

	/**
	 * Returns a String presentation of the Customer
	 * containing information relating to the whole name.
	 *
	 * @return will never be {@literal null}.
	 */
	public String getFullName(){return firstname + " " + lastname;}

	/**
	 * Returns the customer's telephone number,
	 * saved as String.
	 *
	 * @return will never be {@literal null}.
	 */
	public String getTel() { return tel; }

	public void setTel(String tel) { this.tel = tel; }

	/**
	 * Returns the customer's address,
	 * saved as String.
	 *
	 * @return will never be {@literal null}.
	 */
	public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

	/**
	 * Returns the customer's TAN,
	 * needed for verifying the customer.
	 *
	 * @return will never be {@literal null}.
	 */
    public String getTan() { return tan; }

	public void setTan(String tan) { this.tan = tan; }

	/**
	 * Returns a map, which contains the amount of DiningSets a customer rented
	 * as well as their termination date.
	 *
	 * @return will never be {@literal null}.
	 */
	public ConcurrentHashMap<LocalDate, Long> getDiningSets() { return diningSets; }

	/**
	 * Returns the the absolute amount of diningSets a Customer could return.
	 *
	 * @return will never be {@literal null}.
	 */
	public long getCountDiningSets() {

		Iterator<LocalDate> date = getDiningSets().keySet().iterator();
		long count = 0;

		while(date.hasNext()) {
			count = count + getDiningSets().get(date.next());
		}
		return count;
	}
}