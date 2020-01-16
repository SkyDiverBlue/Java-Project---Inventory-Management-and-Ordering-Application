package papapizza.catalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.javamoney.moneta.Money;
import org.salespointframework.quantity.Metric;

import static org.junit.jupiter.api.Assertions.*;

import static org.salespointframework.core.Currencies.EURO;
import static papapizza.catalog.ProductType.*;

public class ItemTest {
	private Item item;
	private Item pizza;

	private Money sPrice = Money.of(4, EURO);
	private Money negSPrice = Money.of(-4, EURO);

	private Money pPrice = Money.of(5, EURO);
	private Money negPPrice = Money.of(-5, EURO);

	private ProductType type = BEVERAGE;

	@BeforeEach
	public void setUp(){
		item = new Item("name", "image", "description", sPrice, pPrice, type);
		pizza = new Item("name", Metric.UNIT, pPrice, type);

	}

	@Test
	public void setNegSPrice(){
		assertThrows(IllegalArgumentException.class,
				()->{
					item = new Item("name", "image", "description", negSPrice, pPrice, type);
				});
	}

	@Test
	public void setNegPPrice(){
		assertThrows(IllegalArgumentException.class,
				()->{
					item = new Item("name", "image", "description", sPrice, negPPrice, type);
				});
	}

	@Test
	public void defaultContructor(){
		new Item();
	}

	@Test
	public void testContructor(){
		assertEquals( "name", item.getName());
		assertEquals("image", item.getImage());
		assertEquals("description", item.getDescription());
		assertEquals(sPrice, item.getPrice());
		assertEquals(type, item.getType());
	}

	@Test
	public void testPizzaConstructor(){
		assertEquals(type, pizza.getType());
		assertEquals("Just a new Pizza, nothing to worry about", pizza.getDescription());
		assertEquals(Money.of(0, EURO), pizza.getPurchasingPrice());
		assertEquals("", pizza.getImage());
	}

	@Test
	//tests all setters of class
	public void setters(){
		item.setImage("image2");
		assertEquals("image2", item.getImage());

		item.setType(TOPPING);
		assertEquals(TOPPING, item.getType());

		item.setDescription("another description");
		assertEquals("another description", item.getDescription());

		item.setPurchasingPrice(Money.of(7, EURO));
		assertEquals(Money.of(7, EURO), item.getPurchasingPrice());

		item.setPrice(Money.of(8, EURO));
		assertEquals(Money.of(8, EURO), item.getPrice());

	}
}
