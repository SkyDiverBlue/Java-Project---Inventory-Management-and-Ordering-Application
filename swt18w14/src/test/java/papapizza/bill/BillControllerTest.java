package papapizza.bill;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import papapizza.bill.OrderRepo;
import papapizza.employee.EmployeeRepository;
import papapizza.office.Office;
import papapizza.order.KindOfPizza;


@SpringBootTest //needed as well
@Transactional
public class BillControllerTest {

	@Autowired
	BillController controller;

	Model model;

	@BeforeEach
	void setUp(){
		model = new ExtendedModelMap();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void completedOrders(){
		String returnedView = controller.complete(model);
		assertEquals(returnedView, "completedOrders");
		assertTrue(model.containsAttribute("completedOrders"));
		assertTrue(model.containsAttribute("bills"));

	}

	@Test
	@WithMockUser(roles = "ORDERMANAGER")
	void readyForPickUp(){
		String returnedView = controller.showAllOrdersReadyForPickUp(model);
		assertEquals(returnedView, "productsReadyForPickUp");
		assertTrue(model.containsAttribute("bills"));
	}

	@Test
	@WithMockUser(roles = "DELIVERER")
	void deliverableProducts(){
		String returnedView = controller.showAllOrdersReadyForDelivery(model);
		assertEquals(returnedView, "deliverableProducts");
		assertTrue(model.containsAttribute("bills"));
	}
}
