package papapizza.inventory;

import org.javamoney.moneta.Money;
import org.salespointframework.accountancy.Accountancy;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.catalog.Product;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.quantity.Quantity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import papapizza.catalog.Item;
import papapizza.office.Office;

import java.util.ArrayList;
import java.util.Iterator;

import static org.salespointframework.core.Currencies.EURO;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_ORDERMANAGER')")
public class InventoryController{

	private final Inventory<InventoryItem> inventory;
	private final Office office;
	private final Accountancy accountancy;

	InventoryController(Inventory<InventoryItem> inventory, Office office, Accountancy accountancy) {
		this.inventory = inventory;
		this.office = office;
		this.accountancy = accountancy;
	}

	@GetMapping("/stock")
	String stock(Model model) {

		ArrayList<InventoryItem> stock = new ArrayList<>();
		inventory.findAll().forEach(inventoryItem -> stock.add(inventoryItem));

		lowonstock(model,stock);

		model.addAttribute("criticalAmount",office.getCriticalAmount());
		System.out.println(office.getCriticalAmount());

		return "stock";
	}

	@GetMapping("/stock/{item}")
	String editQuantity(@PathVariable InventoryItem item, Model model, EditQuantityForm form){

		model.addAttribute("form",form);
		model.addAttribute("item",item);

		return "editQuantity";
	}

	@PostMapping("/editQuantity/{item}")
	String editQuantity(@PathVariable InventoryItem item, EditQuantityForm form, Errors result,
						RedirectAttributes redirectAttributes){

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("editQuantity", false);
			return "redirect:/";
		}

		try{
			if(Integer.parseInt(form.getQuantity())<0){
				redirectAttributes.addFlashAttribute("editQuantity", false);
				return "redirect:/stock";
			}
		}catch(NumberFormatException e){
			redirectAttributes.addFlashAttribute("format", false);
			return "redirect:/stock";
		}

		Quantity f = Quantity.of(Integer.parseInt(form.getQuantity()));
		int amount = f.subtract(item.getQuantity()).getAmount().intValue();

		item.increaseQuantity(f.subtract(item.getQuantity()));
		if (amount > 0) {
			try {
				Item itemProduct = (Item) item.getProduct();
				accountancy.add(new AccountancyEntry(Money.of(0, EURO).subtract(itemProduct.
						getPurchasingPrice().multiply(amount)), " Es wurde das Produkt " +
						itemProduct.getName() + " " + amount + " mal zum Lager hinzugefügt"));
			} catch (Exception e) {
				System.out.println("can't cast " + e);
			}
		}
		inventory.save(item);

		redirectAttributes.addFlashAttribute("editQuantity", true);
		return "redirect:/stock";
	}

	@GetMapping("/searchi")
	String search(@RequestParam("search") String search, Model model) {

		ArrayList<InventoryItem> inventoryItems = new ArrayList<>();

		boolean res=false;

		if(search.equals("")) {
			inventory.findAll().forEach(inventoryItem -> inventoryItems.add(inventoryItem));
		} else{
			inventory.findAll().forEach(inventoryItem -> {
				if(inventoryItem.getProduct().getName().toLowerCase().contains(search.toLowerCase())){
					inventoryItems.add(inventoryItem);
				}
			});
			res=inventoryItems.isEmpty();
			if(res) {
				inventory.findAll().forEach(inventoryItem -> inventoryItems.add(inventoryItem));
			}
		}
		model.addAttribute("noresult",res);
		lowonstock(model,inventoryItems);
		return "stock";
	}

	@PostMapping("/stock")
	String criticalAmount(EditCriticalAmount form){
		office.setCriticalAmount(form.getCriticalAmount());
		return "redirect:/stock";
	}


	private Model lowonstock(Model model, ArrayList<InventoryItem> inventory){
		ArrayList<InventoryItem> stock = new ArrayList<>();
		ArrayList<InventoryItem> zeroStock = new ArrayList<>();
		ArrayList<InventoryItem> lowStock = new ArrayList<>();

		Iterator<InventoryItem> item = inventory.iterator();
		while(item.hasNext()) {
			InventoryItem i = item.next();
			if(i.getQuantity().isLessThan(Quantity.of(1))) zeroStock.add(i);
			else
				if(i.getQuantity().isLessThan(Quantity.of(office.getCriticalAmount()+1))&&i.getQuantity()
					.isGreaterThan(Quantity.of(0))) lowStock.add(i);
			else stock.add(i);
		}

		model.addAttribute("zerostock",zeroStock);
		model.addAttribute("stock",stock);
		model.addAttribute("lowstock",lowStock);

		return model;
	}
}
