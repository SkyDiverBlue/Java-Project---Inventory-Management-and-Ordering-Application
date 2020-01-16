package  papapizza.customer;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.salespointframework.time.BusinessTime;


@Component
@Order(10)
class CustomerDataInitializer implements DataInitializer {

	private final CustomerRepository customers;

	CustomerDataInitializer(CustomerRepository customers) {

		Assert.notNull(customers, "CustomerRepository must not be null!");

		this.customers = customers;
	}

	@Override
	public void initialize() {

		long firstId = 1;

		if (customers.existsById(firstId)) {
			return;
		}

		Customer c1 = new Customer("Juppie", "Juppsen", "035811111");
		Customer c2 = new Customer("Droggel", "Becher", "035815611");

		c1.setAddress("Dort Dastraße 1");
		c2.setAddress("Irgendwo Ebendaweg 5");

		if (customers.count() == 0) {
			customers.saveAll(Arrays.asList(c1, c2));
		}
	}
}