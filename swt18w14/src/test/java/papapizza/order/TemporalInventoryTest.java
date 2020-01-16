package papapizza.order;


import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.order.Cart;

import org.salespointframework.order.CartItem;
import org.salespointframework.quantity.Quantity;
import org.springframework.boot.test.context.SpringBootTest;
import papapizza.catalog.Item;
import papapizza.catalog.ProductType;


import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.salespointframework.core.Currencies.EURO;


@SpringBootTest
public class TemporalInventoryTest {


	private Cart cart = new Cart();
	private TemporalInventory temporalInventory;
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


	@BeforeEach
	public void SetUp(){
		this.temporalInventory = new TemporalInventory();
	}



	@Test
	public void TestEmpyCart(){
		assertEquals(temporalInventory.getStoreEveryThing().size(), 0);
	}

	@Test
	public void TestCartTotalQuantity(){
		cart.addOrUpdateItem(item1, Quantity.of(3));
		cart.addOrUpdateItem(item2, Quantity.of(3));
		cart.addOrUpdateItem(item3, Quantity.of(3));
		cart.addOrUpdateItem(item4, Quantity.of(3));
		cart.addOrUpdateItem(item5, Quantity.of(3));

		temporalInventory.addItemToMap(item1, Quantity.of(3));
		temporalInventory.addItemToMap(item2, Quantity.of(3));
		temporalInventory.addItemToMap(item3, Quantity.of(3));
		temporalInventory.addItemToMap(item4, Quantity.of(3));
		temporalInventory.addItemToMap(item5, Quantity.of(3));


		Quantity totalQuantityCart = Quantity.of(0);
		for (CartItem cartItem: cart.get().toArray(CartItem[]::new)){
			totalQuantityCart = totalQuantityCart.add(cartItem.getQuantity());
		}

		Quantity totalQuanityTemporal = Quantity.of(0);
		for (Map.Entry<Item,Quantity> entry: temporalInventory.getStoreEveryThing().entrySet()){
			totalQuanityTemporal =  totalQuanityTemporal.add(entry.getValue());
		}

		assertEquals(totalQuanityTemporal, totalQuantityCart);
	}

	@Test
	public void testUpdateItem(){
		cart.addOrUpdateItem(item3, Quantity.of(4));
		temporalInventory.addItemToMap(item3, Quantity.of(4));

		CartItem cartItemToTest = null;
		for (CartItem cartItem: cart.get().toArray(CartItem[]::new)){
			if (cartItem.getProductName().equals(item3.getName())){
				cartItemToTest = cartItem;
			}
		}

		assertEquals(cartItemToTest.getQuantity(), temporalInventory.getQuantityForItem(item3));
	}




	@Test
	public void clearAll(){
		temporalInventory.clearAll();

		assertEquals(0, temporalInventory.getStoreEveryThing().size());
	}


	@Test
	public void testQuantityOfItem(){
		Item itemN = new Item("vsd", "Gordon.vsd", "Where is the F*sdv LAMSOUCE",
				Money.of(5,EURO), Money.of(12, EURO),ProductType.TOPPING);

		temporalInventory.addItemToMap(itemN, Quantity.of(4));

		assertEquals(Quantity.of(4), temporalInventory.getQuantityForItem(itemN));

	}

	@Test
	public void testReturnOfNothingExisting(){
		Item itemN = new Item("vsd", "Gordon.vsd", "Where is the F*sdv LAMSOUCE",
				Money.of(5,EURO), Money.of(12, EURO),ProductType.TOPPING);

		assertEquals(Quantity.of(0), temporalInventory.getQuantityForItem(itemN));
	}


	@Test
	public void testBeforeNewEntry(){
		Item itemN = new Item("s", "d.vsd", "d is the F*dd d", Money.of(8,EURO),
				Money.of(12, EURO),ProductType.TOPPING);

		int i = temporalInventory.getStoreEveryThing().size();
		temporalInventory.addItemToMap(itemN, Quantity.of(3));

		assertNotEquals(i, temporalInventory.getStoreEveryThing().size());
	}
	@Test
	public void testAfterNewEntry(){
		Item itemN = new Item("s", "d.vsd", "d is the F*dd d", Money.of(8,EURO),
				Money.of(12, EURO),ProductType.TOPPING);

		temporalInventory.addItemToMap(itemN, Quantity.of(3));
		int i = temporalInventory.getStoreEveryThing().size();

		assertEquals(i, temporalInventory.getStoreEveryThing().size());
	}

	@Test
	public void testReplace(){

		temporalInventory.addItemToMap(item2, Quantity.of(3));
		temporalInventory.addItemToMap(item2, Quantity.of(3));


		assertEquals(Quantity.of(6), temporalInventory.getQuantityForItem(item2));
	}

	@Test
	public void testRemoveNotNull(){
		temporalInventory.addItemToMap(item2, Quantity.of(3));
		temporalInventory.removeItem(item2, Quantity.of(1));


		assertEquals(Quantity.of(2), temporalInventory.getQuantityForItem(item2));
	}

	@Test
	public void testRemoveNull(){
		temporalInventory.addItemToMap(item2, Quantity.of(3));
		temporalInventory.removeItem(item2, Quantity.of(3));


		assertEquals(Quantity.of(0), temporalInventory.getQuantityForItem(item2));
	}
}
