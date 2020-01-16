package papapizza.catalog;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.salespointframework.catalog.Product;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.quantity.Quantity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.junit.jupiter.api.Test;


import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.salespointframework.core.Currencies.EURO;
import static papapizza.catalog.ProductType.*;

@SpringBootTest
@WithMockUser(roles = "ADMIN")
public class CatalogControllerTest {

	@Autowired
	CatalogController catalogController;

	@Autowired
	Inventory<InventoryItem> inventory;

	@Autowired
	ProductCatalog catalog;

	private AddItemForm addItemForm;
	private AddItemForm addItemFormException;
	private EditItemForm editItemForm;
	private EditItemForm editItemFormException;
	private RedirectAttributes emptyRedirect;

	@BeforeEach
	public void setUp(){
		initMocks(this);

		addItemForm = mock(AddItemForm.class);
		given(addItemForm.getName()).willReturn("flageolet");
		given(addItemForm.getPrice()).willReturn("4,5");
		given(addItemForm.getPurchasingPrice()).willReturn("3,5");
		given(addItemForm.getDescription()).willReturn("inspiration");
		given(addItemForm.getImage()).willReturn("prettttyyy");
		given(addItemForm.getType()).willReturn(TOPPING);
		given(addItemForm.getQuantity()).willReturn("5");

		addItemFormException = mock(AddItemForm.class);
		given(addItemFormException.getName()).willReturn("flageolet");
		given(addItemFormException.getPrice()).willReturn("4,5");
		given(addItemFormException.getPurchasingPrice()).willReturn("not a number"); //parameter that will trigger exception
		given(addItemFormException.getDescription()).willReturn("inspiration");
		given(addItemFormException.getImage()).willReturn("prettttyyy");
		given(addItemFormException.getType()).willReturn(TOPPING);
		given(addItemForm.getQuantity()).willReturn("5");

		editItemForm = mock(EditItemForm.class);
		given(editItemForm.getImage()).willReturn("new image");
		given(editItemForm.getName()).willReturn("new name");
		given(editItemForm.getPrice()).willReturn("5,5");
		given(editItemForm.getPurchasingPrice()).willReturn("4,5");

		editItemFormException = mock(EditItemForm.class);
		given(editItemFormException.getImage()).willReturn("new image");
		given(editItemFormException.getName()).willReturn("new name");
		given(editItemFormException.getPrice()).willReturn("not a number");
		given(editItemFormException.getPurchasingPrice()).willReturn("4,5");

		emptyRedirect = mock(RedirectAttributes.class);
		when(emptyRedirect.getFlashAttributes()).thenReturn(Collections.emptyMap());
	}



	@Test
	public void catalog(){
		Model model = new ExtendedModelMap();
		String returnedView = catalogController.catalog(model);

		assertThat(returnedView).isEqualTo("productList");
	}

	@Test
	public void addItem(){
		Model model = new ExtendedModelMap();
		String returnedView = catalogController.addItem(model, null);
		assertThat(returnedView).isEqualTo("addItem");
	}

	@Test
	public void addNewItem(){
		String returnedView = catalogController.addNewItem(addItemForm, emptyRedirect);
		assertThat(returnedView).isEqualTo("redirect:/productList");
	}

	@Test
	public void addNewItemAlreadyExists(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		redirectAttributes.addFlashAttribute("error", "Produktname bereits vergeben");
		String returnedView = catalogController.addNewItem(addItemForm, redirectAttributes);
	}

	@Test
	public void addNewItemException(){
		String returnedView = catalogController.addNewItem(addItemFormException, emptyRedirect);
		assertThat(returnedView).isEqualTo("redirect:/addItem");
	}

	@Test
	void deleteItem(){
		RedirectAttributes re = new RedirectAttributesModelMap();
		Item item = new Item("ananaan", "cjfghju", "sfztzzg", Money.of(1, EURO), Money.of(2, EURO), SALAD);
		catalog.save(item);
		inventory.save(new InventoryItem(item, Quantity.of(1)));
		String returnedView = catalogController.deleteItem(item, re);
		assertEquals(returnedView, "redirect:/productList");
		assertEquals(re.getFlashAttributes().get("succes"), item.getName()+" wurde erfolgreich gelöscht");
	}

	@Test
	public void edit(){
		Model model = new ExtendedModelMap();
		Item beverage = new Item("beverage", "image1", "", Money.of(0, EURO),
				Money.of(0, EURO), BEVERAGE);
		String returnedView = catalogController.edit(beverage, model, null);
		assertThat(returnedView).isEqualTo("editItem");
	}

	@Test
	public void editItem(){
		Item item = new Item("Ham", "test","diced Ham pieces",
				Money.of(3.99, EURO) , Money.of(2.99, EURO), TOPPING);
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = catalogController.editItem(editItemForm, item, redirectAttributes);

		assertThat(returnedView).isEqualTo("redirect:/productList");
	}

	@Test
	public void editItemException(){
		Item item = new Item("Ham", "test","diced Ham pieces",
				Money.of(3.99, EURO) , Money.of(2.99, EURO), TOPPING);
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = catalogController.editItem(editItemFormException, item, redirectAttributes);

		assertTrue(redirectAttributes.getFlashAttributes().containsKey("error"));
		assertThat(returnedView).isEqualTo("redirect:/products/" + item.getId());
	}

	@Test
	@WithAnonymousUser // used because of @PreAuthorize("!isAuthenticated()")
	public void toppings(){
		Model model = new ExtendedModelMap();
		String returnedView = catalogController.toppings(model);
		assertThat(returnedView).isEqualTo("catalog");
	}

	@Test
	@WithAnonymousUser
	public void beverages(){
		Model model = new ExtendedModelMap();
		String returnedView = catalogController.beverages(model);
		assertThat(returnedView).isEqualTo("catalog");
	}

	@Test
	@WithAnonymousUser
	public void salads(){
		Model model = new ExtendedModelMap();
		String returnedView = catalogController.salads(model);
		assertThat(returnedView).isEqualTo("catalog");
	}

	@Test
	public void searchProductListEmptyString(){
		Model model = new ExtendedModelMap();
		String returnedView = catalogController.searchProductList("", model);
		assertThat(returnedView).isEqualTo("productList");
	}

	@Test
	public void searchProductList(){
		Model model = new ExtendedModelMap();
		String returnedView = catalogController.searchProductList("b", model);
		assertThat(returnedView).isEqualTo("productList");
	}

	@Test
	@WithAnonymousUser
	public void search(){
		Model model = new ExtendedModelMap();
		String returnedView = catalogController.search("", model);
		assertThat(returnedView).isEqualTo("catalog");
	}

	@Test
	public void testnameError(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		catalogController.testname(redirectAttributes, "impossible String Does Not Exists");

		assertFalse(redirectAttributes.getFlashAttributes().containsValue("error"));
	}

	@Test
	public void testName(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		catalogController.testname(redirectAttributes, "Ham");

		assertTrue(redirectAttributes.getFlashAttributes().containsKey("error"));
	}

}
