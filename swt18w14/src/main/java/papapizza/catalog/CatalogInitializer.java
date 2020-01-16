package papapizza.catalog;

import static org.salespointframework.core.Currencies.*;
import static papapizza.catalog.ProductType.*;

import org.javamoney.moneta.Money;
import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Order(20)
class CatalogInitializer implements DataInitializer {

	private final ProductCatalog productCatalog;

	CatalogInitializer(ProductCatalog productCatalog) {

		Assert.notNull(productCatalog, "ProductCatalog must not be null!");

		this.productCatalog = productCatalog;
	}


	@Override
	public void initialize() {




		if (productCatalog.count()>0) {
			return;
		}


		//Toppings
		productCatalog.save(new Item("Salami", "top", "just some basic salami",
				Money.of(2.99, EURO) , Money.of(1.99, EURO), TOPPING));


		productCatalog.save(new Item("Ham", "top","diced Ham pieces",
				Money.of(3.99, EURO) , Money.of(2.99, EURO), TOPPING));
		productCatalog.save(new Item("Pineapple", "top","not on a Pizza, you monster",
				Money.of(0.99, EURO) , Money.of(1.20, EURO), TOPPING));
		productCatalog.save(new Item("Crème chantilly", "top", "to go with the pineapple",
				Money.of(0.75, EURO), Money.of(0.99, EURO), TOPPING));
		productCatalog.save(new Item("Tomato sauce", "top",
				"to-MAY-to or  to-MAH-to, that is the question",
				Money.of(0.75, EURO), Money.of(0.99, EURO), TOPPING));
		productCatalog.save(new Item("Truffles", "top",
				"menu item to impress your friends, you show-off",
				Money.of(25.99, EURO), Money.of(30.99, EURO), TOPPING));
		productCatalog.save(new Item("Arsenic", "top",
				"we do not take any legal responsibility for any ill effects this product might cause to the consumer",
				Money.of(3.99, EURO), Money.of(4.5, EURO), TOPPING));
		//Beverages
		productCatalog.save(new Item("Cola", "bev", "mix with Mentos for best taste",
				Money.of(1.5, EURO) , Money.of(2.5, EURO), BEVERAGE));
		productCatalog.save(new Item("Sprite", "bev", "water, sugar with artificial aromas",
				Money.of(1.75, EURO), Money.of(2.25, EURO), BEVERAGE));
		productCatalog.save(new Item("Hohes C", "bev", "C; C++ or C#? Choose your flavor",
				Money.of(1.55, EURO), Money.of(2.25, EURO), BEVERAGE));
		productCatalog.save(new Item("Sterni", "bev", "Don't lie, you don't like it, but hell it's cheap",
				Money.of(0.99, EURO), Money.of(1.25, EURO), BEVERAGE));

		//Salad
		productCatalog.save(new Item("Salad", "sal",
				"We don't have any salad jokes, so if you have any lettuce know", Money.of(5.99, EURO),
				Money.of(6.25, EURO), SALAD));
		productCatalog.save(new Item("Bacon salad", "sal",
				"No vegetables were harmed in the making of this product", Money.of(5.99, EURO),
				Money.of(6.25, EURO), SALAD));

		//DiningSets
		productCatalog.save(new Item("Dining-Set", "test", "Bring them back alive",
				Money.of(15.00, EURO), Money.of(5.00, EURO),DINING_SET));
	}

}
