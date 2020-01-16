package papapizza.order;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.catalog.Product;
import org.salespointframework.quantity.Metric;
import org.salespointframework.quantity.Quantity;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import papapizza.catalog.Item;
import papapizza.catalog.PizzaProduct;
import papapizza.catalog.ProductType;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.salespointframework.core.Currencies.EURO;

@SpringBootTest
@WithMockUser(roles = "ORDERMANAGER")
public class KindOfPizzaTest {

	private KindOfPizza kindOfPizza;
	private PizzaProduct pizzaProduct = new PizzaProduct("new  Pizza", Metric.UNIT, ProductType.PIZZA);
	private LocalDateTime localDateTime = LocalDateTime.now();
	private String userAccountId = "who's your Daddy?";
	private Item item1;

	@BeforeEach
	public void createNewPizza(){

		pizzaProduct.addTopping(new Item("Cheese", "irgendwas.jpeg", "IT#S CHEESE",
				Money.of(3,EURO), Money.of(5, EURO),ProductType.TOPPING) , Quantity.of(3));
		pizzaProduct.addTopping(new Item("Salt", "saltyBoy.jpeg", "do you now de way",
				Money.of(4,EURO), Money.of(45, EURO),ProductType.TOPPING) , Quantity.of(3));
		pizzaProduct.addTopping(new Item("Apple", "asap.jpeg", "THIS IS UGANDAAA",
				Money.of(2,EURO), Money.of(3.5, EURO),ProductType.TOPPING) , Quantity.of(3));
		pizzaProduct.addTopping(new Item("Cheese Creme", "irgendwas.jpeg",
				"Cremechesse... its cheese... and creme", Money.of(13,EURO), Money.of(24, EURO),
				ProductType.TOPPING) , Quantity.of(3));
		pizzaProduct.addTopping(new Item("SOUCE", "Gordon.jpeg", "Where is the F*CKING LAMSOUCE",
				Money.of(5,EURO), Money.of(12, EURO),ProductType.TOPPING) , Quantity.of(3));

		kindOfPizza = new KindOfPizza(localDateTime, "abcdefgh", pizzaProduct.getPrice().toString(), pizzaProduct.getName());

		item1 = new Item("SOUCE", "Gordon.jpeg", "Where is the F*CKING LAMSOUCE",
				Money.of(5,EURO), Money.of(12, EURO),ProductType.TOPPING);
		for (Map.Entry<Product, Integer> entry: pizzaProduct.getToppings().entrySet()){
			kindOfPizza.addToppings(entry.getKey().getName(), entry.getValue());
		}

		kindOfPizza.setUserAcId(userAccountId);
	}

	@Test
	public void testNull(){
		assertNotNull(kindOfPizza);
	}

	@Test
	public void testPrice(){
		assertEquals(pizzaProduct.getPrice().toString(), kindOfPizza.getPrice());
	}

	@Test
	public void testTime(){
		assertEquals(localDateTime, kindOfPizza.getTime());
	}

	@Test
	public void testUserAcc(){
		assertEquals(userAccountId, kindOfPizza.getUserAcId());
		kindOfPizza.setUserAcId("TEST");
		assertEquals("TEST", kindOfPizza.getUserAcId());
	}

	@Test
	public void testAddSomething(){
		pizzaProduct.addTopping(item1, Quantity.of(3));
		kindOfPizza.addToppings(item1.getName(), 3);

		assertEquals(kindOfPizza.getToppings().size(), pizzaProduct.getToppings().size());
	}



	@Test
	public void testUUID(){
		assertNotNull( kindOfPizza.getUUID());
	}

	@Test
	public void testNewName(){
		assertNotEquals(-3, kindOfPizza.getId());
	}

	@Test
	public void testRelatedOrder(){
		String relatedOrder= "ffff";
		KindOfPizza KOP = new KindOfPizza(LocalDateTime.now(), relatedOrder, "rr", "ege" );
		assertEquals("ffff", KOP.getRelatedOrder());
	}


	@Test
	public void testName(){
		String relatedOrder= "ffff";
		KindOfPizza KOP = new KindOfPizza(LocalDateTime.now(), relatedOrder, "rr", "ege" );
		assertEquals("ege", KOP.getName());
	}




	@Test
	public void testSetTime(){
		String relatedOrder= "ffff";
		KindOfPizza KOP = new KindOfPizza(LocalDateTime.now(), relatedOrder, "rr", "ege" );

		LocalDateTime localDateTime = LocalDateTime.now();

		KOP.setTime(localDateTime);

		assertEquals(localDateTime, KOP.getTime());
	}

	@Test
	public void testDefaultConstructor(){
		assertNotNull(new KindOfPizza());
	}
}
