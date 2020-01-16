package papapizza.order;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.quantity.Quantity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import papapizza.baking.BakingManagement;
import papapizza.bill.OrderRepo;
import papapizza.catalog.Item;
import papapizza.catalog.ProductCatalog;
import papapizza.coupons.CouponsRepo;
import papapizza.customer.Customer;
import papapizza.customer.CustomerManagement;
import papapizza.office.Office;

import static  org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.salespointframework.core.Currencies.EURO;
import static papapizza.catalog.ProductType.*;

@SpringBootTest
@WithMockUser(roles = "ORDERMANAGER")
public class CartControllerTest{

	@Autowired
	CartController cartController;

	@Autowired
	Inventory<InventoryItem> inventory;

	@Autowired
	ProductCatalog productCatalog;


	TemporalInventory temporalInventory;

	//all mocked classes for CartController constructor
	private CustomerManagement customerManagement;
	private Office office;
	private BakingManagement bakingManagement;
	private CouponsRepo couponsRepo;
	private OrderRepo orderRepo;

	//Variables needed for testing
	private Item beverage;
	private Item topping;
	private Item salad;
	private Item diningSet;

	private Money sPrice = Money.of(3.00, EURO);
	private Money pPrice = Money.of(4.00, EURO);

	private Cart cartToEmpy;
	private Customer customer;

	//Setting up test environment
	@BeforeEach
	public void setUp(){
		initMocks(this);
		customer = new Customer("Jane", "Doe", "123");
		customerManagement = mock(CustomerManagement.class);
		office = mock(Office.class);
		bakingManagement = mock(BakingManagement.class);
		couponsRepo = mock(CouponsRepo.class);
		orderRepo = mock(OrderRepo.class);

		cartController = new CartController(inventory, customerManagement, office, bakingManagement, couponsRepo, orderRepo);

		when(customerManagement.getVerifiedCustomer()).thenReturn(customer);

		//makes sure inventory is empty before repopulating it
		inventory.deleteAll();

		beverage = new Item("beverage", "image1", "", sPrice, pPrice, BEVERAGE);
		productCatalog.save(beverage);
		InventoryItem beverageInv = new InventoryItem(beverage, Quantity.of(0));
		inventory.save(beverageInv);

		topping = new Item("topping", "image2", "", sPrice, pPrice, TOPPING);
		productCatalog.save(topping);
		InventoryItem toppingInv = new InventoryItem(topping, Quantity.of(5));
		inventory.save(toppingInv);

		salad = new Item("salad", "image3", "", sPrice, pPrice, SALAD);
		productCatalog.save(salad);
		InventoryItem saladInv = new InventoryItem(salad, Quantity.of(5));
		inventory.save(saladInv);

		diningSet = new Item("diningSet", "image4", "", sPrice, pPrice, DINING_SET);
		productCatalog.save(diningSet);
		InventoryItem diningSetInv = new InventoryItem(diningSet, Quantity.of(5));
		inventory.save(diningSetInv);

		//setting up cart to clear
		cartToEmpy = new Cart();
		cartToEmpy.addOrUpdateItem(beverage, 3);
	}

	//testing  @ModelAttribute("cart") is a session attribute
	@Test
	public void sessionAttributes(){
		assertEquals(Cart.class, cartController.initializeCart().getClass());
	}

	//testing the @GetMapping("/orderScreen/cart")
	@Test
	public void cart(){
		assertEquals("cart", cartController.cart());
	}

	//testing @GetMapping("/orderScreen/add")
	@Test
	public void addItemSuccessful(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = cartController.addItem(salad, cartToEmpy, redirectAttributes);

		for(CartItem cartItem : cartToEmpy){
			if(cartItem.getProduct() == salad){
				assertEquals(Quantity.of(1), cartItem.getQuantity());
			}
		}

		assertThat(returnedView).isEqualTo(cartController.getReturn(salad));
		assertTrue(redirectAttributes.getFlashAttributes().containsKey("niceStock"));
	}

	@Test
	public void addItemUnsuccessful(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = cartController.addItem(beverage, cartToEmpy, redirectAttributes);


		assertTrue(redirectAttributes.getFlashAttributes().containsKey("stockEmpty"));
		assertThat(returnedView).isEqualTo(cartController.getReturn(beverage));
	}

	@Test
	public void removeItemSuccessful(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		cartController.addItem(salad, cartToEmpy, redirectAttributes); //adding to delete

		String returnedView = cartController.removeItem(salad, cartToEmpy, redirectAttributes);

		assertTrue(redirectAttributes.getFlashAttributes().containsKey("inCart"));
		assertThat(returnedView).isEqualTo(cartController.getReturn(salad));
	}

	@Test
	public void removeItemUnsuccessful(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = cartController.removeItem(salad, cartToEmpy, redirectAttributes);

		System.out.println(redirectAttributes.getFlashAttributes());
		//assertTrue(redirectAttributes.getFlashAttributes().containsKey("notInCart"));
		assertThat(returnedView).isEqualTo(cartController.getReturn(salad));
	}

	@Test
	public void confirmOrderPickUpFreeBeverage(){
		String decider = "takeItYourself";
		Cart cart = mock(Cart.class);
		when(cart.getPrice()).thenReturn(Money.of(35, EURO));
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = cartController.confirmOrder(decider, cart, redirectAttributes);

		assertFalse(cartController.getOrder().getDeliverableState());
		assertThat(returnedView).isEqualTo("redirect:freeBeverage");
	}

	@Test
	public void confirmOrderDeliver(){
		String decider = "toDeliver";
		Cart cart = mock(Cart.class);
		when(cart.getPrice()).thenReturn(Money.of(15, EURO));

		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = cartController.confirmOrder(decider, cart, redirectAttributes);

		assertTrue(cartController.getOrder().getDeliverableState());
		assertThat(returnedView).isEqualTo("redirect:complete");
	}

	//testing clearCart, @GetMapping("/orderScreen/clear")
	@Test
	public void clearCart(){
		String returnView = cartController.clearCart(cartToEmpy);
		assertThat(returnView).isEqualTo("redirect:/");
		assertTrue(cartToEmpy.isEmpty());
	}

	//testing switch cases in getReturn()
	@Test
	public void getReturn(){
		assertEquals(cartController.getReturn(beverage), "redirect:/orderScreen/beverages");
		assertEquals(cartController.getReturn(topping), "redirect:/orderScreen/toppings");
		assertEquals(cartController.getReturn(salad), "redirect:/orderScreen/salads");
		assertEquals(cartController.getReturn(diningSet), "redirect:/orderScreen/diningSets");
	}

	@Test
	public void completeIsDiscounted(){

		Model model = new ExtendedModelMap();
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

		cartController.confirmOrder("takeItYourself", cartToEmpy, redirectAttributes);
		cartController.getOrder().setGuessedDeliveryTime(5L);
		cartController.getOrder().setCoupon(true); //makes isDiscounted() true

		String returnedView = cartController.complete(model);

		assertEquals(true, ((ExtendedModelMap) model).get("discounted"));
		assertTrue(((ExtendedModelMap) model).containsKey("discount"));
		assertThat(returnedView).isEqualTo("complete");
	}

	@Test
	public void completeNotDiscounted(){
		Model model = new ExtendedModelMap();
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

		cartController.confirmOrder("toDeliver", cartToEmpy, redirectAttributes);
		cartController.getOrder().setGuessedDeliveryTime(5L);
		cartController.getOrder().setCoupon(false); //makes isDiscounted() false

		String returnedView = cartController.complete(model);

		assertEquals(false, ((ExtendedModelMap) model).get("discounted"));
		assertThat(returnedView).isEqualTo("complete");
	}


	@Test
	public void getFreeBeverageUnsuccessful(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnView = cartController.getFreeBeverage(beverage, cartToEmpy, redirectAttributes);
		assertThat(returnView).isEqualTo("redirect:/orderScreen/freeBeverage");
	}


}
