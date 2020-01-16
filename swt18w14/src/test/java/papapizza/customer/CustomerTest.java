package papapizza.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerTest {

	private Customer customer;

	@BeforeEach
	public void setUp(){
		customer = new Customer("John", "Smith", "1234");
	}

	@Test
	public void controller(){
		assertEquals("John", customer.getFirstname());
		assertEquals("Smith", customer.getLastname());
		assertEquals("1234", customer.getTel());
		assertEquals("INITIAL", customer.getTan());
		assertEquals(0, customer.getCountDiningSets());
	}

	@Test
	public void setters(){
		customer.setFirstname("Jane");
		assertEquals("Jane", customer.getFirstname());

		customer.setLastname("Doe");
		assertEquals("Doe", customer.getLastname());

		customer.setTel("0491");
		assertEquals("0491", customer.getTel());

		customer.setTan("456");
		assertEquals("456", customer.getTan());

		customer.setAddress("bored lane");
		assertEquals("bored lane", customer.getAddress());

		customer.getDiningSets().put(LocalDate.now(),(long) 5);
		assertEquals(5, customer.getCountDiningSets());
	}

	@Test //this is such a dumb test (but coverage I guess)
	public void getFullName(){
		assertEquals(customer.getFirstname() + " " + customer.getLastname(), customer.getFullName());
	}

}
