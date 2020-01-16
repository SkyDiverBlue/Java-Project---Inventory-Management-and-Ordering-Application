package papapizza.catalog;

import org.javamoney.moneta.Money;
import org.salespointframework.quantity.Metric;

import static org.salespointframework.core.Currencies.EURO;

public class FreeBeverage extends Item {
	
	public FreeBeverage(String Name, Metric metric, ProductType productType){
			super(Name, metric, Money.of(0, EURO), ProductType.BEVERAGE);
	}

	@Override
	public Money getPurchasingPrice(){
		return Money.of(0, EURO);
	}

}
