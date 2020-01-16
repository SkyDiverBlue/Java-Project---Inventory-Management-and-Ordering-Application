package papapizza.catalog;


import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.salespointframework.quantity.Metric;
import org.salespointframework.quantity.Quantity;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.salespointframework.core.Currencies.EURO;

@SpringBootTest
public class PizzaProductTest {

	private PizzaProduct pizzaProduct;
	private Item item1 = new Item("Cheese", "irgendwas.jpeg", "IT#S CHEESE",
			Money.of(3, EURO), Money.of(5,EURO), ProductType.TOPPING);
	private Item item2 = new Item("Salt", "saltyBoy.jpeg", "do you now de way",
			Money.of(4,EURO), Money.of(45, EURO),ProductType.TOPPING);
	private Item item3 = new Item("Apple", "asap.jpeg", "THIS IS UGANDAAA", Money.of(2,EURO),
			Money.of(3.5, EURO),ProductType.TOPPING);
	private Item item4 = new Item("Cheese Creme", "irgendwas.jpeg", "Cremechesse... its cheese... and creme",
			Money.of(13,EURO), Money.of(24, EURO),ProductType.TOPPING);
	private Item item5 = new Item("SOUCE", "Gordon.jpeg", "Where is the F*CKING LAMSOUCE",
			Money.of(5,EURO), Money.of(12, EURO),ProductType.TOPPING);




	@Test
	public void testAddNew(){
		pizzaProduct = new PizzaProduct("pizza", Metric.UNIT, ProductType.PIZZA);
		pizzaProduct.addTopping(item2, Quantity.of(3));

		assertEquals(1, pizzaProduct.getToppings().size());
	}

	@Test
	public void testAddExisting(){
		pizzaProduct = new PizzaProduct("pizza", Metric.UNIT, ProductType.PIZZA);
		pizzaProduct.addTopping(item2, Quantity.of(3));
		pizzaProduct.addTopping(item2, Quantity.of(5));

		assertEquals(1, pizzaProduct.getToppings().size());
	}


	@Test
	public void testDecrease(){
		pizzaProduct = new PizzaProduct("pizza", Metric.UNIT, ProductType.PIZZA);
		pizzaProduct.addTopping(item2, Quantity.of(3));
		pizzaProduct.addTopping(item3, Quantity.of(3));
		pizzaProduct.decreaseProductAmmount(item2, Quantity.of(2));

		assertEquals(2, pizzaProduct.getToppings().size());
	}

	@Test
	public void testDelete(){
		pizzaProduct = new PizzaProduct("pizza", Metric.UNIT, ProductType.PIZZA);
		pizzaProduct.addTopping(item2, Quantity.of(3));
		pizzaProduct.decreaseProductAmmount(item2, Quantity.of(5));

		assertEquals(0, pizzaProduct.getToppings().size());
	}

	@Test
	public void testRemove(){
		pizzaProduct = new PizzaProduct("pizza", Metric.UNIT, ProductType.PIZZA);
		pizzaProduct.addTopping(item2, Quantity.of(3));
		pizzaProduct.removeProduct(item2);

		assertEquals(0, pizzaProduct.getToppings().size());
	}

	@Test
	public void testQuantity(){
		pizzaProduct = new PizzaProduct("pizza", Metric.UNIT, ProductType.PIZZA);
		pizzaProduct.addTopping(item2, Quantity.of(3));


		assertEquals(Quantity.of(3), pizzaProduct.getQuantityOfProduct(item2));
	}

	@Test
	public void testPrice(){
		pizzaProduct = new PizzaProduct("pizza", Metric.UNIT, ProductType.PIZZA);
		pizzaProduct.addTopping(item2, Quantity.of(3));
		pizzaProduct.addTopping(item3, Quantity.of(3));

		assertEquals(Money.of(18, EURO), pizzaProduct.getPrice());
	}

	/*@Test
	public void testTime(){
		pizzaProduct = new PizzaProduct("pizza", Metric.UNIT, ProductType.PIZZA);

		assertEquals(LocalDateTime.now(), pizzaProduct.getTime());
	}*/
}
