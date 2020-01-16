package papapizza.baking;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import papapizza.bill.OrderRepo;
import papapizza.customer.Customer;
import papapizza.employee.EmployeeRepository;
import papapizza.order.CustomerOrder;
import papapizza.order.KindOfPizza;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
public class BakingManagementTest {

	@Autowired
	BakingController controller;

	@Autowired
	BakingManagement management;

	@Autowired
	PizzaRepo pizzaRepo;

	@Autowired
	UserAccountManager userAccounts;

	@Autowired
	EmployeeRepository employees;

	@Autowired
	BusinessTime businessTime;

	private UserAccount u1;
	private UserAccount u2;


	@BeforeEach
	public void setUp(){
		if(employees.count()<=5){
			u1 = userAccounts.create("baker1", "123", Role.of("ROLE_BAKER"));
			u2 = userAccounts.create("baker2", "123", Role.of("ROLE_BAKER"));
			userAccounts.save(u1);
			userAccounts.save(u2);
		}
	}

	@Test
	void findPizzaByUUIDTest(){
		KindOfPizza pizza = new KindOfPizza(businessTime.getTime(), "order1", "25.40", "Palermo");
		management.addToPizzaRepo(pizza);
		assertEquals(management.findPizzaByUUID(pizza.getUUID()), pizza);
		assertNull(management.findPizzaByUUID("gibtesnicht"));
	}

	@Test
	public void rejectsUnauthenticatedAccessToController() {

		assertThatExceptionOfType(AuthenticationException.class) //
				.isThrownBy(() -> controller.toBeBaked(new ExtendedModelMap()));
	}

	@Test
	void bakingBakerTest(){

		KindOfPizza pizza = new KindOfPizza(businessTime.getTime(), "order1", "25.40", "Palermo");

		management.addToPizzaRepo(pizza);
		management.putInOven(u1.getId(), pizza);

		Model model = new ExtendedModelMap();

		Model model1 = management.bakingBaker(u1.getId(), model);
		Model model2 = management.bakingBaker(u2.getId(), model);

		assertTrue(model.containsAttribute("alreadyBaking"));
		assertTrue(model.containsAttribute("nextPizza"));
	}

	@Test
	public void nextPizzaToBeBakedNoPizza(){

		management.clearPizzaRepo();

		KindOfPizza pizza = management.nextPizza();
		assertNull(pizza);
	}

	@Test
	public void nextPizzaToBeBakedPos(){

		management.addToPizzaRepo(new KindOfPizza(businessTime.getTime(), "order1", "25.40", "Palermo"));
		management.addToPizzaRepo(new KindOfPizza(businessTime.getTime().plusYears(1), "order2", "6.99", "Atlanta"));
		management.addToPizzaRepo(new KindOfPizza(businessTime.getTime().minusMinutes(5), "order3", "20.99", "Venedig"));



		KindOfPizza pizza = management.nextPizza();
		assertEquals(pizza.getName(), "Venedig");
	}

	@Test
	public void toBeBakedAmountZeroNotInOven(){

		management.clearPizzaRepo();

		assertEquals(management.toBeBakedAmount(), 0);
	}

	@Test
	public void toBeBakedAmountZeroSthInOven(){

		management.clearPizzaRepo();

		KindOfPizza pizza = new KindOfPizza(businessTime.getTime(), "order1", "25.40", "Palermo");

		management.addToPizzaRepo(pizza);
		management.putInOven(u1.getId(), pizza);

		assertEquals(management.toBeBakedAmount(), 0);
	}

	@Test
	public void	toBeBakedAmountMore(){

		management.clearPizzaRepo();

		management.addToPizzaRepo(new KindOfPizza(businessTime.getTime(), "order1", "25.40", "Palermo"));
		management.addToPizzaRepo(new KindOfPizza(businessTime.getTime(), "order2", "6.99", "Atlanta"));

		assertEquals(management.toBeBakedAmount(), 2);
	}

	@Test
	public void inOvenUpdate(){

		management.clearPizzaRepo();

		KindOfPizza pizza = new KindOfPizza(businessTime.getTime(), "order1", "25.40", "Palermo");
		KindOfPizza pizza2 = new KindOfPizza(businessTime.getTime(), "order1", "6.99", "Atlanta");

		management.addToPizzaRepo(pizza);
		management.addToPizzaRepo(pizza2);

		pizza.setTime(businessTime.getTime().plusMinutes(5));
		pizza.setUserAcId(u1.getId().getIdentifier());
		pizza2.setTime(businessTime.getTime());
		pizza2.setUserAcId(u2.getId().getIdentifier());
		management.addToPizzaRepo(pizza);
		management.addToPizzaRepo(pizza2);

		management.inOvenUpdate();

		assertEquals(management.toBeBakedAmount(), 0);
		assertEquals(management.inOvenAmount(), 1);
	}

	@Test
	public void inOvenAmount(){

		management.clearPizzaRepo();

		KindOfPizza pizza = new KindOfPizza(businessTime.getTime(), "order1", "25.40", "Palermo");
		KindOfPizza pizza2 = new KindOfPizza(businessTime.getTime(), "order1", "6.99", "Atlanta");
		KindOfPizza pizza3 = new KindOfPizza(businessTime.getTime(), "order2", "9.99", "Venedig");



		management.addToPizzaRepo(pizza);
		management.putInOven(u1.getId(), pizza);
		management.addToPizzaRepo(pizza2);
		management.putInOven(u2.getId(), pizza2);
		management.addToPizzaRepo(pizza3);

		assertEquals(management.inOvenAmount(), 2);
	}

	//already bakin!!
}