package papapizza.catalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.javamoney.moneta.Money;
import org.salespointframework.quantity.Metric;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

import static org.salespointframework.core.Currencies.EURO;
import static papapizza.catalog.ProductType.*;

public class FreeBeverageTest {

	private FreeBeverage item;

	private Money sPrice = Money.of(4, EURO);
	private Money negSPrice = Money.of(-4, EURO);

	private Money pPrice = Money.of(5, EURO);
	private Money negPPrice = Money.of(-5, EURO);

	private ProductType type = BEVERAGE;



	@BeforeEach
	public void setUp(){
		item = new FreeBeverage("name", Metric.UNIT, type);
	}

	@Test
	void purchasingPrice(){
		assertEquals(item.getPurchasingPrice(), Money.of(0, EURO));
	}
}
