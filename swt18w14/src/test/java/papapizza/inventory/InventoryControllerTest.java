package papapizza.inventory;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.quantity.Quantity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import papapizza.catalog.Item;
import papapizza.catalog.ProductCatalog;
import papapizza.office.Office;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.salespointframework.core.Currencies.EURO;
import static papapizza.catalog.ProductType.TOPPING;


@SpringBootTest
@WithMockUser(roles = "ADMIN")
public class InventoryControllerTest {

	@Autowired
	InventoryController inventoryController;

	@Autowired
	ProductCatalog productCatalog;

	@Autowired
	Office office;

	private Item item;
	private InventoryItem invItem;

	private EditQuantityForm quantityForm;
	private EditQuantityForm negativeQuantityForm;
	private EditQuantityForm NFEQuantityForm;
	private EditCriticalAmount criticalAmountForm;

	private Errors hasErrors;
	private Errors noErrors;

	@BeforeEach
	public void setUp(){

		item = new Item("name", "image","description",
				Money.of(3, EURO) , Money.of(4, EURO), TOPPING);
		productCatalog.save(item); //needed to avoid: object references an unsaved transient instance

		invItem = new InventoryItem(item, Quantity.of(10));

		quantityForm = mock(EditQuantityForm.class);
		given(quantityForm.getQuantity()).willReturn("4");

		negativeQuantityForm = mock(EditQuantityForm.class);
		given(negativeQuantityForm.getQuantity()).willReturn("-4");

		NFEQuantityForm = mock(EditQuantityForm.class);
		given(NFEQuantityForm.getQuantity()).willReturn("-e");

		criticalAmountForm = mock(EditCriticalAmount.class);
		given(criticalAmountForm.getCriticalAmount()).willReturn(2L);

		hasErrors = mock(Errors.class);
		given(hasErrors.hasErrors()).willReturn(true);

		noErrors = mock(Errors.class);
		given(noErrors.hasErrors()).willReturn(false);
	}


	@Test
	public void stock(){
		Model model = new ExtendedModelMap();
		String returnView = inventoryController.stock(model);

		assertThat(returnView).isEqualTo("stock");
	}

	@Test
	public void editQuantityView(){
		Model model = new ExtendedModelMap();
		String returnedView = inventoryController.editQuantity(null, model, quantityForm);
		assertThat(returnedView).isEqualTo("editQuantity");
	}

	@Test
	public void editQuantityErrors(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = inventoryController.editQuantity(invItem, quantityForm, hasErrors, redirectAttributes);

		assertEquals(false, redirectAttributes.getFlashAttributes().get("editQuantity"));
		assertThat(returnedView).isEqualTo("redirect:/");
	}

	@Test
	public void editQuantityNoErrorsInvalid(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = inventoryController.editQuantity(invItem, negativeQuantityForm, noErrors, redirectAttributes);

		assertEquals(false, redirectAttributes.getFlashAttributes().get("editQuantity"));
		assertThat(returnedView).isEqualTo("redirect:/stock");
	}

	@Test
	public void editQuantityNumberFormatException(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = inventoryController.editQuantity(invItem, NFEQuantityForm, noErrors, redirectAttributes);

		assertEquals(false, redirectAttributes.getFlashAttributes().get("format"));
		assertThat(returnedView).isEqualTo("redirect:/stock");

	}

	@Test
	public void editQuantityNoErrorsValid(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = inventoryController.editQuantity(invItem, quantityForm, noErrors, redirectAttributes);

		assertEquals(true, redirectAttributes.getFlashAttributes().get("editQuantity"));
		assertThat(returnedView).isEqualTo("redirect:/stock");
		assertEquals(quantityForm.getQuantity(), invItem.getQuantity().toString());
	}

	@Test
	public void searchEmptyStrg(){
		Model model = new ExtendedModelMap();
		String returnedView = inventoryController.search("", model);

		assertTrue(model.containsAttribute("noresult"));
		assertThat(returnedView).isEqualTo("stock");
	}

	@Test
	public void search(){
		Model model = new ExtendedModelMap();
		String returnedView = inventoryController.search("n", model);

		assertTrue(model.containsAttribute("noresult"));
		assertThat(returnedView).isEqualTo("stock");
	}

	@Test
	public void searchImpossible(){
		Model model = new ExtendedModelMap();
		String returnedView = inventoryController.search("impossible Search", model);

		assertTrue(model.containsAttribute("noresult"));
		assertThat(returnedView).isEqualTo("stock");
	}

	@Test
	public void criticalAmount(){
		String returnedView = inventoryController.criticalAmount(criticalAmountForm);

		assertEquals(criticalAmountForm.getCriticalAmount().longValue(), office.getCriticalAmount());
		assertThat(returnedView).isEqualTo("redirect:/stock");
	}

}
