package papapizza.office;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.accountancy.Accountancy;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


@SpringBootTest
@WithMockUser(roles = "ADMIN")
class OfficeControllerTest {

	@Autowired
	OfficeController officeController;

	private Model notNuLLmodel = new ExtendedModelMap();

	private OvenForm ovenForm;
	private Errors noErrors;
	private Errors hasErrors;

	//Mocked classes for constructor
	private Accountancy accountancy;
	private BusinessTime businessTime;
	private  Office office;

	@BeforeEach
	void setUp(){
		initMocks(this);
		accountancy = mock(Accountancy.class);
		businessTime = mock(BusinessTime.class);
		office = mock(Office.class);

		officeController = new OfficeController(accountancy, businessTime, office);

		hasErrors = mock(Errors.class);
		given(hasErrors.hasErrors()).willReturn(true);

		noErrors = mock(Errors.class);
		given(noErrors.hasErrors()).willReturn(false);

		ovenForm = mock(OvenForm.class);
	}

	@Test
	public void wasPaid(){
		notNuLLmodel.addAttribute("notNUll", "notNull");
		officeController.wasPaid(notNuLLmodel);
	}

	@Test
	public void ovenFullUsage(){
		Model model = new ExtendedModelMap();
		String message = "Alle Öfen werden durch Bäcker bedient.";

		when(office.getUtilization()).thenReturn(0);

		String returnedView = officeController.oven(model);
		assertEquals(message, ((ExtendedModelMap) model).get("utilization"));
		assertThat(returnedView).isEqualTo("oven");
	}

	@Test
	public void ovenBaker(){
		Model model = new ExtendedModelMap();
		String message = "Es gibt " + 5 + " Bäcker ohne Ofen.";

		when(office.getUtilization()).thenReturn(5);

		String returnedView = officeController.oven(model);
		assertEquals(message, ((ExtendedModelMap) model).get("utilization"));
		assertThat(returnedView).isEqualTo("oven");
	}

	@Test
	public void ovenOvens(){
		Model model = new ExtendedModelMap();
		String message = "Es gibt " + 4 + " unbediente Öfen.";

		when(office.getUtilization()).thenReturn(-4);

		String returnedView = officeController.oven(model);
		assertEquals(message, ((ExtendedModelMap) model).get("utilization"));
		assertThat(returnedView).isEqualTo("oven");
	}


	@Test
	public void editOvensHasErrors(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = officeController.editovens(ovenForm, hasErrors, redirectAttributes);

		assertEquals(true, redirectAttributes.getFlashAttributes().get("error"));
		assertThat(returnedView).isEqualTo("redirect:/oven");
	}

	@Test
	public void editOvensNoErrors(){
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = officeController.editovens(ovenForm, noErrors, redirectAttributes);

		assertEquals(false, redirectAttributes.getFlashAttributes().get("error"));
		assertThat(returnedView).isEqualTo("redirect:/oven");
	}
/*
	@Test
	void changeIntervall(){
		Model model = new ExtendedModelMap();
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
		String returnedView = officeController.changeInterval("01-01-2019", "10-01-2019", model);
		assertEquals(returnedView, "office");
		assertTrue(model.containsAttribute("accountings"));
		assertTrue(model.containsAttribute("accountancy"));
		assertTrue(model.containsAttribute("format"));
	}
*/

}
