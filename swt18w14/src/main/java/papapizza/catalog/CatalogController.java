package papapizza.catalog;

import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.quantity.Quantity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

import java.util.ArrayList;

import static org.salespointframework.core.Currencies.EURO;

@Controller
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class CatalogController {

	private static final Quantity NONE = Quantity.of(0);

	private final ProductCatalog productCatalog;
	private final Inventory<InventoryItem> inventory;

	CatalogController(ProductCatalog productCatalog, Inventory<InventoryItem> inventory) {

		Assert.notNull(inventory,"inventory must not be null!");
		Assert.notNull(productCatalog, "productCatalog must not be null!");

		this.productCatalog = productCatalog;
		this.inventory = inventory;
	}

	@GetMapping("/productList")
	String catalog(Model model) {

		model.addAttribute("items",productCatalog.findAll());

		return "productList";
	}

	@GetMapping("/addItem")
	String addItem(Model model, AddItemForm form) {
		model.addAttribute("form", form);
		model.addAttribute("topping",ProductType.TOPPING);
		model.addAttribute("beverage",ProductType.BEVERAGE);
		model.addAttribute("salad",ProductType.SALAD);

		return "addItem";
	}

	@PostMapping("/addItem")
	String addNewItem(@Valid AddItemForm form, RedirectAttributes redirectAttributes) {
		try {

			testname(redirectAttributes,form.getName());
			if(!redirectAttributes.getFlashAttributes().isEmpty()) return "redirect:/addItem";

			String pPrice = form.getPurchasingPrice();
			String sPrice = form.getPrice();
			sPrice = sPrice.replace(",",".");
			pPrice = pPrice.replace(",",".");

			Money sellingPrice = Money.of(Float.parseFloat(sPrice),EURO);
			Money purchasingPrice = Money.of(Float.parseFloat(pPrice),EURO);

			Item item = new Item(form.getName(), form.getImage(), form.getDescription(), sellingPrice, purchasingPrice,
					form.getType());
			productCatalog.save(item);
			inventory.save(new InventoryItem(item, Quantity.of(Integer.parseInt(form.getQuantity()))));
		}catch(Exception e) {
			redirectAttributes.addFlashAttribute("error", "Fehler bei der Eingabe");
			return "redirect:/addItem";
		}

		return "redirect:/productList";
	}


	@GetMapping("/products/{item}")
	String edit(@PathVariable Item item, Model model, EditItemForm form) {

		model.addAttribute("form", form);
		model.addAttribute("item", item);
		model.addAttribute("purchase",item.getPurchasingPrice().toString().substring(4));
		model.addAttribute("sell",item.getPrice().toString().substring(4));

		return "editItem";
	}

	@PostMapping("/editItem/{item}")
	String editItem(@Valid EditItemForm form, @PathVariable Item item, RedirectAttributes redirectAttributes) {

		try {

			item.setName(form.getName());
			item.setDescription(form.getDescription());
			String pPrice = form.getPurchasingPrice();
			String sPrice = form.getPrice();
			sPrice = sPrice.replace(",",".");
			pPrice = pPrice.replace(",",".");

			Money sellingPrice = Money.of(Float.parseFloat(sPrice),EURO);
			Money purchasingPrice = Money.of(Float.parseFloat(pPrice),EURO);

			item.setPrice(sellingPrice);
			item.setPurchasingPrice(purchasingPrice);

			if(sellingPrice.isNegative()||purchasingPrice.isNegative()){throw new Exception();}

			item.setImage(form.getImage());
		}catch(Exception e){
			redirectAttributes.addFlashAttribute("error", "Fehler bei der Eingabe");
			return "redirect:/products/"+item.getId();
		}
		productCatalog.save(item);

		return "redirect:/productList";
	}

	@GetMapping("/products/{item}/delete")
	String deleteItem(@PathVariable Item item, RedirectAttributes redirectAttributes){
		if(item.getType() == ProductType.DINING_SET){
			redirectAttributes.addFlashAttribute("diningSet", true);
			return "redirect:/productList";
		}

		inventory.delete(inventory.findByProduct(item).get());
		productCatalog.delete(item);
		redirectAttributes.addFlashAttribute("succes",item.getName()+" wurde erfolgreich gelöscht");
		return "redirect:/productList";
	}

	@GetMapping("/searchp")
	String searchProductList(@RequestParam("search") String search, Model model) {

		searchf(model,search);

		return "productList";
	}

	@GetMapping("/toppings")
	@PreAuthorize("!isAuthenticated()")
	String toppings(Model model) {

		model.addAttribute("noresult",false);
		model.addAttribute("items", productCatalog.findByType(ProductType.TOPPING));
		model.addAttribute("title","Papa Pizza!");

		return "catalog";
	}

	@GetMapping("/beverages")
	@PreAuthorize("!isAuthenticated()")
	String beverages(Model model) {

		model.addAttribute("noresult",false);
		model.addAttribute("items", productCatalog.findByType(ProductType.BEVERAGE));
		model.addAttribute("title","Papa Pizza!");

		return "catalog";
	}

	@GetMapping("/salads")
	@PreAuthorize("!isAuthenticated()")
	String salads(Model model) {
		model.addAttribute("noresult",false);
		model.addAttribute("items", productCatalog.findByType(ProductType.SALAD));
		model.addAttribute("title","Papa Pizza!");

		return "catalog";
	}

	@GetMapping("/searchca")
	@PreAuthorize("!isAuthenticated()")
	String search(@RequestParam("search") String search,Model model) {

		searchf(model,search);
		model.addAttribute("title", "Suchergebnis");
		return "catalog";
	}

	private Model searchf (Model model, String search){
		ArrayList<Item> products = new ArrayList<>();
		boolean res=false;

		if(search.equals("")) {
			model.addAttribute("items", productCatalog.findAll());
		} else{
			productCatalog.findAll().forEach(item -> {
				if(item.getName().toLowerCase().contains(search.toLowerCase())){
					products.add(item);
				}
			});
			res=products.isEmpty();
			model.addAttribute("items",products);
		}
		model.addAttribute("noresult",res);
	 	return model;
	}

	public RedirectAttributes testname(RedirectAttributes redirectAttributes, String totest){
		for (Item item: 	(productCatalog.findAll())
		) {
			if(totest.equals(item.getName())) {
				redirectAttributes.addFlashAttribute("error", "Produktname bereits " + "vergeben");
			}
		}
		return redirectAttributes;
	}
}

