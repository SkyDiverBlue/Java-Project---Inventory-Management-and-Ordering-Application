package papapizza.order;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WithMockUser(roles = "ORDERMANAGER")
public class OrderControllerTest {

	@Autowired
	OrderController orderController;

	private Model model = new ExtendedModelMap();

	@Test
	public void orderableProducts(){
		String returnView = orderController.orderableProducts(model);
		assertThat(returnView).isEqualTo("orderScreen");
	}

	@Test
	public void toppings(){
		String returnView = orderController.toppings(model);
		assertThat(returnView).isEqualTo("orderScreen");
	}

	@Test
	public void beverages(){
		String returnView = orderController.beverages(model);
		assertThat(returnView).isEqualTo("orderScreen");
	}

	@Test
	public void salads(){
		String returnView = orderController.salads(model);
		assertThat(returnView).isEqualTo("orderScreen");
	}

	@Test
	public void diningSets(){
		String returnView = orderController.diningSets(model);
		assertThat(returnView).isEqualTo("orderScreen");
	}

	@Test
	public void freeBeverage(){
		String returnView = orderController.freeBeverage(model);
		assertThat(returnView).isEqualTo("freeBeverage");
	}
}
