package papapizza.order;

import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManager;
import org.salespointframework.order.OrderStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.security.access.prepost.PreAuthorize;
import papapizza.catalog.ProductCatalog;

import static papapizza.catalog.ProductType.*;

@Controller
@PreAuthorize("hasRole('ROLE_ORDERMANAGER')")
@SessionAttributes("cart")
public class OrderController {

	private final ProductCatalog orderableProducts;
	private final OrderManager<Order> orderManager;

	OrderController(ProductCatalog orderableProducts, OrderManager<Order> orderManager){
		this.orderableProducts = orderableProducts;
		this.orderManager = orderManager;
	}

	@GetMapping("orderScreen")
	String orderableProducts(Model model) {
		model.addAttribute("orderScreen", orderableProducts.findAll());
		return "orderScreen";
	}

	@GetMapping("orderScreen/toppings")
	String toppings(Model model) {
		model.addAttribute("orderScreen", orderableProducts.findByType(TOPPING));
		return "orderScreen";
	}

	@GetMapping("orderScreen/beverages")
	String beverages(Model model) {
		model.addAttribute("orderScreen", orderableProducts.findByType(BEVERAGE));
		return "orderScreen";
	}

	@GetMapping("orderScreen/salads")
	String salads(Model model) {
		model.addAttribute("orderScreen", orderableProducts.findByType(SALAD));
		return "orderScreen";
	}

	@GetMapping("orderScreen/diningSets")
	String diningSets(Model model) {
		model.addAttribute("orderScreen", orderableProducts.findByType(DINING_SET));
		return "orderScreen";
	}

	@GetMapping("orderScreen/freeBeverage")
	String freeBeverage(Model model){
		System.out.println("beverages are coming");
		model.addAttribute("order", orderableProducts.findByType(BEVERAGE));
		return "freeBeverage";
	}
}
