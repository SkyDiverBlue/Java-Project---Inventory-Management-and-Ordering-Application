package papapizza.order;

import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.order.*;
import org.salespointframework.quantity.Metric;
import org.salespointframework.quantity.Quantity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import papapizza.baking.BakingManagement;
import papapizza.bill.OrderRepo;
import papapizza.catalog.*;
import papapizza.coupons.Coupon;
import papapizza.coupons.CouponsRepo;
import papapizza.customer.Customer;
import papapizza.customer.CustomerManagement;
import papapizza.office.Office;

import java.time.LocalDateTime;
import java.util.*;

import static org.salespointframework.core.Currencies.EURO;

/**
 * A Spring MVC controller to manage the {@link Cart}.
 */
@Controller
@SessionAttributes("cart")
@PreAuthorize("hasRole('ROLE_ORDERMANAGER')")
public class  CartController {


	private final Inventory<InventoryItem> inventory;
	private final CustomerManagement customerManagement;
	private final Office office;
	private final BakingManagement bakingManagement;
	private final CouponsRepo couponsRepo;
	private final OrderRepo orderRepo;
	private PizzaProduct pizzaProduct;
	private CustomerOrder order;
	private final TemporalInventory temporalInventory;

	/**
	 * Constructor
	 * Creates a new {@link CartController} with the given {@link Inventory}, {@link CustomerManagement},
	 * {@link Office}, {@link BakingManagement}, {@link CouponsRepo}, {@link OrderRepo}.
	 */
	CartController(Inventory<InventoryItem> inventory, CustomerManagement customerManagement, Office office,
				   BakingManagement bakingManagement, CouponsRepo couponsRepo, OrderRepo orderRepo){

		Assert.notNull(inventory, "Inventory must not be null!");
		Assert.notNull(customerManagement, "CustomerManagement must not be null!");
		Assert.notNull(office, "Office must not be null!");
		Assert.notNull(bakingManagement, "BakingManagement must not be null!");
		Assert.notNull(couponsRepo, "CouponsRepo must not be null!");
		Assert.notNull(orderRepo, "OrderRepo must not be null!");

		this.inventory = inventory;
		this.customerManagement = customerManagement;
		this.office = office;
		this.bakingManagement = bakingManagement;
		this.couponsRepo = couponsRepo;
		this.orderRepo = orderRepo;
		this.pizzaProduct = new PizzaProduct("pizza", Metric.UNIT, ProductType.PIZZA);
		temporalInventory = new TemporalInventory();
	}

	/**
	 * initializes the cart
	 * @return
	 */
	@ModelAttribute("cart")
	Cart initializeCart() {
		return new Cart();
	}

	/**
	 *
	 * @return the mapping of the cart
	 */
	@GetMapping("/orderScreen/cart")
	String cart() {
		return "cart";
	}

	/**
	 * checks if the inventory has the sufficient Quantity and stores it in the temp inventory
	 * @param item
	 * @param cart
	 * @param redirectAttributes
	 * @return to the same mapping it had before
	 */
	@GetMapping("/orderScreen/add")
	public String addItem(@RequestParam("p_id") Item item, @ModelAttribute Cart cart,
						  RedirectAttributes redirectAttributes) {
		InventoryItem invItem = inventory.findByProduct(item).get();

		if(item.getType() == ProductType.TOPPING && office.getOven() == 0) {
			redirectAttributes.addFlashAttribute("noOven" , "Es sind derzeit keine Öfen vorhanden um Pizzen zu backen...");

			return "redirect:/orderScreen";
		}

		Quantity totalQuantity = temporalInventory.getQuantityForItem(item).add(Quantity.of(1));
		if (invItem.getQuantity().isGreaterThanOrEqualTo(totalQuantity)){
			if (item.getType() == ProductType.TOPPING) {
				if(pizzaProduct.getToppings().size() == 0) {
					pizzaProduct.setName(item.getName() + "-Pizza");
				}
				if(pizzaProduct.getToppings().size() == 1) {
					pizzaProduct.setName(pizzaProduct.getToppings().firstKey().getName()+ "-" + item.getName() + "-Pizza");
				}
				pizzaProduct.addTopping(item, Quantity.of(1));       //Pizza wird erst am Ende eingefügt, daher sonderregellung hier
			}
				temporalInventory.addItemToMap(item, Quantity.of(1));
				cart.addOrUpdateItem(item, Quantity.of(1));
				redirectAttributes.addFlashAttribute("niceStock", item.getName() + " wurde zur Bestellung hinzugefügt.");
				return getReturn(item);

		}
		redirectAttributes.addFlashAttribute("stockEmpty", "Es gibt derzeit keine " + item.getName() + " mehr.");
		return getReturn(item);
	}

	/**
	 * removes an item or decreases the quantity of an item
	 * @param item
	 * @param cart
	 * @param redirectAttributes
	 * @return to the same mapping it had before
	 */
	//remove one Item from cart function
	@GetMapping("/orderScreen/remove")
	public String removeItem(@RequestParam("p_id") Item item, @ModelAttribute Cart cart,
							 RedirectAttributes redirectAttributes) {
		CartItem cartItem = cart.addOrUpdateItem(item, 0);

		if (cartItem.getQuantity().equals(Quantity.of(0))){
			cart.removeItem(cartItem.getId());
			System.out.println("werde nicht hinzugefügt");
			return getReturn(item);
		}

		InventoryItem inventoryItem = inventory.findByProduct(item).get();

		inventoryItem.getQuantity();
		if(cartItem.getQuantity().isGreaterThan(Quantity.of(1))){
			if (item.getType() == ProductType.TOPPING) {
				pizzaProduct.decreaseProductAmmount(item, Quantity.of(1));
				System.out.println(pizzaProduct.getQuantityOfProduct(item));
			}
			cart.addOrUpdateItem(item, -1);
			temporalInventory.removeItem(item, Quantity.of(1));
			redirectAttributes.addFlashAttribute("inCart", item.getName() + " wurde aus der Bestellung entfernt.");
		} else if (cartItem.getQuantity().equals(Quantity.of(1))){
			if (item.getType() == ProductType.TOPPING) {
				pizzaProduct.decreaseProductAmmount(item, Quantity.of(1));
			}
			cart.removeItem(cartItem.getId());
			temporalInventory.removeItem(item, Quantity.of(1));
			redirectAttributes.addFlashAttribute("inCart", item.getName() + " wurde aus der Bestellung entfernt.");

		} else {
			System.out.println("kann nichts löschen, was ich nicht habe");
			redirectAttributes.addFlashAttribute("notInCart", "Es ist gar kein(e) " + item.getName() + " in der Bestellung.");
		}

		return getReturn(item);
	}


	/**
	 * clears the complete cart
	 * @param cart
	 * @return
	 */
	@GetMapping("/orderScreen/clear")
	public String clearCart(@ModelAttribute Cart cart) {
		for(CartItem cartItem: cart.stream().toArray(CartItem[]::new)) {
			Item item = (Item) cartItem.getProduct();
			if (item.getType() == ProductType.PIZZA) {
				PizzaProduct pizza = (PizzaProduct) item;
				workWithPizzaProduct(pizza);
			}
		}
		temporalInventory.clearAll();
		cart.clear();

		return "redirect:/";
	}

	/**
	 * reduces the quantity of the item in the cart to 0
	 * @param cartItemID
	 * @param cart
	 * @return
	 */
	@GetMapping("/cart/delete")
	public String deleteItem(@RequestParam("p_id")  String cartItemID, @ModelAttribute("cart") Cart cart) {
		CartItem cartItem = null;
		Item item = null;
		try {
			cartItem = cart.getItem(cartItemID).get();
			item = (Item) cartItem.getProduct();
		}
		catch (Exception e){
			System.out.println(e);
		}
		 item = (Item) cartItem.getProduct();

		if (item.getType().equals(ProductType.PIZZA)){
			PizzaProduct pizza = (PizzaProduct) item;
			workWithPizzaProduct(pizza);
			cart.removeItem(cartItemID);
		} else {
			if (item.getType() == ProductType.TOPPING){
				pizzaProduct.removeProduct(item);
			}
			temporalInventory.removeItem(item, cartItem.getQuantity());
			cart.removeItem(cartItemID);
		}

		return getReturn(item);
	}

	/**
	 * creates the order and gives her some attributes
	 * @param decider boolean if the order is getting delivered or picked up
	 * @param cart
	 * @return
	 */
	@GetMapping("/orderScreen/orderConfirm")
	public String confirmOrder(@RequestParam("decide")  String decider, @ModelAttribute Cart cart, RedirectAttributes redirectAttributes) {

		if (cart.isEmpty()){
			redirectAttributes.addFlashAttribute("emptyOrder", true);
			return "redirect:/orderScreen";
		}
		else {

			Customer customer = customerManagement.getVerifiedCustomer();
			order = new CustomerOrder(customer);

			if (decider.equals("takeItYourself")) {
				order.setDeliverableState(false);
			} else {
				order.setDeliverableState(true);
			}

			if (cart.getPrice().isGreaterThanOrEqualTo(Money.of(30, EURO))) {

				int diningSetPrices = 0;
				for (CartItem cartItem : cart.stream().toArray(CartItem[]::new)) {
					Item item = (Item) cartItem.getProduct();
					if (item.getType() == ProductType.DINING_SET) {
						diningSetPrices = cartItem.getQuantity().getAmount().intValue();
					}
				}
				System.out.println(Money.of(30, EURO) + "  " + (cart.getPrice().subtract(Money.of(15 * diningSetPrices, EURO))) + " " + diningSetPrices);
				if (Money.of(30, EURO).isLessThanOrEqualTo(cart.getPrice().subtract(Money.of(15 * diningSetPrices, EURO)))) {
					System.out.println("okay zu viele DiningSet");
					return "redirect:freeBeverage";                                        //BEi mehr als 30 Euro, bekomme Gratisgetränk
				}
			}
			createOrder(cart);
		}
		return "redirect:complete";
	}


	/**
	 * adds a pizza to the cart, removes all toppings from the cart
	 * @param cart
	 * @param redirectAttributes
	 * @return
	 */
	@GetMapping("/orderScreen/newPizza")
	public String addNewPizza(@ModelAttribute Cart cart, RedirectAttributes redirectAttributes){

		if (!pizzaProduct.getToppings().isEmpty()) {
			UpdateCart(cart);
			cart.addOrUpdateItem(pizzaProduct, Quantity.of(1));
		}

		pizzaProduct = new PizzaProduct(pizzaProduct.getName(), Metric.UNIT, ProductType.PIZZA);

		redirectAttributes.addFlashAttribute("pizza", "Die neue Pizza: " + pizzaProduct.getName() + ", wurde erstellt.");

		return "redirect:/orderScreen";
	}


	/**
	 * a free beverage is added, if the total price is above 30
	 * adds new item as free item
	 * @param item
	 * @param cart
	 * @param redirectAttributes
	 * @return
	 */
	@GetMapping("/orderScreen/addOneFreeBeverage")
	public String getFreeBeverage(@RequestParam("p_id") Item item, @ModelAttribute Cart cart,
								  RedirectAttributes redirectAttributes) {

		System.out.println("okay wir sind hier " + cart.getPrice() + " und Item " + item.getDescription());
		InventoryItem invItem = inventory.findByProduct(item).get();

		if (invItem.getQuantity().isGreaterThanOrEqualTo(Quantity.of(1))){
			invItem.decreaseQuantity(Quantity.of(1));
			inventory.save(invItem);

			FreeBeverage freeBeverage = new FreeBeverage(item.getName()+ " gratis", Metric.UNIT, ProductType.BEVERAGE);
			System.out.println(freeBeverage.getPurchasingPrice() + freeBeverage.getName());
			//item.setPrice(Money.of(0,EURO));			//geht bei nur einem Getränk dieser Sorte
			cart.addOrUpdateItem(freeBeverage, Quantity.of(1));
			createOrder(cart);
			return "redirect:/orderScreen/complete";
		} else{
			redirectAttributes.addFlashAttribute("stockEmpty", "Es gibt derzeit keine " + item.getName() + " mehr.");
			return "redirect:/orderScreen/freeBeverage";
		}

	}

	//Hilfsklassen, entfernen von duplikaten etc///////

	/**
	 * Adds all information to the order
	 * @param cart
	 */
	private void createOrder(Cart cart){
		if(order==null) {
			System.out.println("NOTNULL");
			return;
		}
		if (!pizzaProduct.getToppings().isEmpty()){
			UpdateCart(cart);
			cart.addOrUpdateItem(pizzaProduct, Quantity.of(1));
		}

		cart.addItemsTo(order);
		order.isNew();
		customerManagement.newTan(order.getCustomer());
		boolean containsPizza = false;

		int amountDiningSet = 0;
		for(CartItem cartItem: cart.stream().toArray(CartItem[]::new)) {
			Item item = (Item) cartItem.getProduct();
			if (item.getType().equals(ProductType.DINING_SET)){
				amountDiningSet = cartItem.getQuantity().getAmount().intValue();
			}
			if (item.getType().equals(ProductType.PIZZA)){
				containsPizza = true;
				PizzaProduct p = (PizzaProduct) item;
				p.setRelatedOrder(order.getId().getIdentifier());
				order.increaseAmount();

				KindOfPizza kindOfPizza = new KindOfPizza(LocalDateTime.now(),
						order.getId().getIdentifier(), p.getPrice().toString(), p.getName());
				for (Map.Entry<Product, Integer> entry: p.getToppings().entrySet()){
					kindOfPizza.addToppings(entry.getKey().getName(), entry.getValue());
				}
				bakingManagement.addToPizzaRepo(kindOfPizza);
			}

			if (item.getType().equals(ProductType.DINING_SET)){
				customerManagement.addDiningSets(cartItem.getQuantity().getAmount().longValue(), order.getCustomer());
			}

		}


		order.setDiningAmount(amountDiningSet);
		System.out.println(amountDiningSet + " diningsets");
		if (!containsPizza){
			order.isNowCompleted();
		}
		for (Map.Entry<Item, Quantity> entry: temporalInventory.getStoreEveryThing().entrySet()){
			InventoryItem inventoryItem = inventory.findByProduct(entry.getKey()).get();
			inventoryItem.decreaseQuantity(entry.getValue());
			inventory.save(inventoryItem);
		}

		cart.clear();
		temporalInventory.clearAll();
		order.setGuessedDeliveryTime(this.amountOfTime());
		orderRepo.save(order);
		pizzaProduct = new PizzaProduct("Pizza", Metric.UNIT, ProductType.PIZZA);
	}


	/**
	 * removes the items from the cart to add them onto the pizza
	 * @param cart
	 */
	private void UpdateCart(Cart cart){
		for (Map.Entry<Product,Integer> entry : pizzaProduct.getToppings().entrySet()){
			Quantity quantity = Quantity.of(0).subtract(Quantity.of(entry.getValue()));
			cart.addOrUpdateItem(entry.getKey(), quantity);
			if (cart.addOrUpdateItem(entry.getKey(), Quantity.of(0)).getQuantity().isLessThan(Quantity.of(1))){
				cart.removeItem(cart.addOrUpdateItem(entry.getKey(), entry.getValue()).getId());
			}
		}
	}

	/**
	 * returns a string to get the correct mapping
	 * @param item
	 * @return
	 */
	public String getReturn(Item item){
		switch (item.getType()) {
			case BEVERAGE:
				return "redirect:/orderScreen/beverages";
			case TOPPING:
				return "redirect:/orderScreen/toppings";
			case SALAD:
				return "redirect:/orderScreen/salads";
			case DINING_SET:
				return "redirect:/orderScreen/diningSets";
			default:
				return  "redirect:/orderScreen";
		}
	}

	/**
	 * shows all information about the order
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/orderScreen/complete", method = RequestMethod.GET)
	String complete(Model model) {
		model.addAttribute("products", order.getOrderLines().get().toArray());
		model.addAttribute("time", order.getGuessedDeliveryTime());
		model.addAttribute("total",order.getTotalPrice());
		if (order.isDiscounted()) {
			model.addAttribute("discounted" , order.isDiscounted());
			model.addAttribute("discount", order.getDiscount());
		} else {
			model.addAttribute("discounted", order.isDiscounted());
		}
		return "complete";
	}

	/**
	 * return to the main page
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/orderScreen/returnHome", method = RequestMethod.GET)
	public String returnHome(Model model){
		System.out.println("ret");
		return "redirect:/";
	}

	/**
	 * searches in the couponRepo after the given Code and adds a new attribute to the order
	 * @param code
	 * @return
	 */
	@RequestMapping(value = "/orderScreen/findByCode", method = RequestMethod.GET)
	public String findByCode(@RequestParam("code") String code, RedirectAttributes redirectAttributes){
		if (order.isCoupon()){
			redirectAttributes.addFlashAttribute("hasCoupon", true);
		}
		else {
			for (Coupon coupon : couponsRepo.findAll()) {
				if (coupon.tryToFindAFittingCode(code)) {
					System.out.println(couponsRepo.count() + " vor dem löschen " + order.getTotalPrice());
					order.setCoupon(true);
					couponsRepo.delete(coupon);
					System.out.println(couponsRepo.count() + " nach dem löschen " + order.getTotalPrice());
					orderRepo.save(order);
					if(order.getDeliverableState()) {
						redirectAttributes.addFlashAttribute("setCoupon", true);
					}else{
						redirectAttributes.addFlashAttribute("setCoupon", false);
					}
				}
			}
			if(!order.isCoupon()){
				redirectAttributes.addFlashAttribute("isCoupon", false);
			}
		}
		return "redirect:complete";
	}

	/**
	 * determines the time the order will take to be completed and ready for delivery
	 * @return
	 */
	private long amountOfTime(){
		long x;
		if(order.getAmountOfPizzen() == 0) {
			if(order.getDeliverableState()) {
				return office.includeDeliveryTime(0);
			} else {
				return 0;
			}
		}

		if (bakingManagement.inOvenAmount()>0) {
			x = 5;
		} else {
			x = 0;
		}
		System.out.println(x);
		if (bakingManagement.toBeBakedAmount()%office.getOven()==0) {
			x = x + (bakingManagement.toBeBakedAmount()/office.getOven())*5;
		} else {
			x = x + (bakingManagement.toBeBakedAmount()/(office.getOven())+1)*5;
		}
		x = office.includeDeliveryTime(x);

		System.out.println(x);
		return x;
	}

	/**
	 * removes or adds the quantity of each product related to a pizza to the inventory
 	 * @param pizza
	 */
	private void workWithPizzaProduct(PizzaProduct pizza){
		for(Map.Entry<Product, Integer> entry :pizza.getToppings().entrySet()){
			temporalInventory.removeItem((Item)entry.getKey(), Quantity.of(entry.getValue()));
		}
	}

	/**
	 * returns the created order
	 * @return order
	 */
	//for testing purposes, returns current CustomerOrder
	public CustomerOrder getOrder(){
		return order;
	}

}
