package papapizza.baking;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import papapizza.bill.OrderRepo;
import papapizza.employee.EmployeeRepository;
import papapizza.office.Office;
import papapizza.order.KindOfPizza;


@SpringBootTest //needed as well
@Transactional
public class BakingControllerTest {

	@Autowired
	BakingManagement management;

	@Autowired
	EmployeeRepository employees;

	@Autowired
	UserAccountManager userAccounts;

	@Autowired
	BakingController controller;

	@Autowired
	BusinessTime businessTime;

	@Autowired
	OrderRepo orderRepo;

	@Autowired
	Office office;

	private Model model = new ExtendedModelMap();
	private Model model2 = new ExtendedModelMap();
	private RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
	private UserAccount u1;
	private KindOfPizza pizza;


	@BeforeEach
	public void setUp(){

		if(employees.count()<=5){
			u1 = userAccounts.create("baker1", "123", Role.of("ROLE_BAKER"));
			userAccounts.save(u1);
			pizza = new KindOfPizza(businessTime.getTime(), "order1", "25.40", "Palermo");
			management.addToPizzaRepo(pizza);
		}
	}

	@Test
	@WithMockUser(roles = "BAKER")
	public void nexPizzaTestAlreadyBaking(){
		model2.addAttribute("alreadyBaking", true);
		String returnedView = controller.nextPizza(model2, u1);
		assertEquals(returnedView, "nextPizza");
	}

	@Test
	@WithMockUser(roles = "BAKER")
	public void toBeBaked(){
		String returnedView = controller.toBeBaked(model);
		assertThat(returnedView).isEqualTo("toBeBaked");
	}

	@Test
	@WithMockUser(roles = "BAKER")
	public void nextPizzaTestPlain(){
		String returnedView = controller.nextPizza(model, u1);

		assertThat(returnedView).isEqualTo("nextPizza");
	}

	@Test
	@WithMockUser(roles = "BAKER")
	public void nextPizzaTestNoPizza(){

		office.setOven(1);
		management.addToPizzaRepo(pizza);
		pizza.setUserAcId("usacid");
		pizza.setTime(businessTime.getTime());

		String returnedView = controller.nextPizza(model, u1);

		assertEquals(returnedView, "nextPizza");
		assertEquals(false, ((ExtendedModelMap) model).get("button"));
		assertNull(((ExtendedModelMap) model).get("nextPizza"));
		assertEquals(false, ((ExtendedModelMap) model).get("alreadyBaking"));
		assertFalse(model.containsAttribute("noOven"));
		assertTrue(model.containsAttribute("noPizza"));
	}

	@Test
	@WithMockUser(roles = "BAKER")
	public void nextPizzaTestNoOven(){

		office.setOven(0);

		String returnedView = controller.nextPizza(model, u1);

		assertEquals(returnedView, "nextPizza");

		assertEquals(false, ((ExtendedModelMap) model).get("button"));
		assertEquals(false, ((ExtendedModelMap) model).get("alreadyBaking"));
		assertFalse(model.containsAttribute("pizza"));
		assertEquals("Es gibt derzeit keinen freien Ofen, der bedient werden könnte.",
				((ExtendedModelMap) model).get("noOven"));
	}

	@Test
	@WithMockUser(roles = "BAKER")
	public void nextPizzaButtonTestNice(){

		office.setOven(1);
		management.addToPizzaRepo(pizza);

		String returnedView = controller.nextPizza(pizza.getUUID(), model, u1);

		assertEquals(returnedView, "nextPizza");
		assertEquals(false, ((ExtendedModelMap) model).get("button"));
		assertEquals(false, ((ExtendedModelMap) model).get("alreadyBaking"));
		assertFalse(model.containsAttribute("noOven"));
	}

	@Test
	@WithMockUser(roles = "BAKER")
	public void nextPizzaButtonTestNoOven(){

		office.setOven(0);

		String returnedView = controller.nextPizza(pizza.getUUID(), model, u1);

		assertEquals(returnedView, "nextPizza");

		assertEquals(true, ((ExtendedModelMap) model).get("button"));
		assertEquals(false, ((ExtendedModelMap) model).get("alreadyBaking"));
		assertFalse(model.containsAttribute("pizza"));
		assertEquals("Es gibt derzeit keinen freien Ofen, der bedient werden könnte.",
				((ExtendedModelMap) model).get("noOven"));
	}

	@Test
	@WithMockUser(roles = "BAKER")
	public void nextPizzaButtonTestBaking(){

		management.addToPizzaRepo(pizza);
		management.putInOven(u1.getId(), pizza);
		office.setOven(1);

		String returnedView = controller.nextPizza(pizza.getUUID(), model, u1);

		assertEquals(returnedView, "nextPizza");

		assertEquals(false, ((ExtendedModelMap) model).get("button"));
		assertEquals(true, ((ExtendedModelMap) model).get("alreadyBaking"));
		assertFalse(model.containsAttribute("noOven"));
		assertTrue(model.containsAttribute("nextPizza"));
	}


	@Test
	@WithMockUser(roles = "BAKER")
	public void inOvenControllerTest(){

		String returnedView = controller.inOven(model);

		assertThat(returnedView).isEqualTo("inOven");

	}
}
