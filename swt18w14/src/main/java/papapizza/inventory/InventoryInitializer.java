package papapizza.inventory;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.quantity.Quantity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import papapizza.catalog.ProductCatalog;

@Component
@Order(20)
class InventoryInitializer implements DataInitializer {

	private final Inventory<InventoryItem> inventory;
	private final ProductCatalog productCatalog;

	InventoryInitializer(Inventory<InventoryItem> inventory, ProductCatalog productCatalog) {

		Assert.notNull(inventory, "Inventory must not be null!");
		Assert.notNull(productCatalog, "ProductCatalog must not be null!");

		this.inventory = inventory;
		this.productCatalog = productCatalog;
	}

	@Override
	public void initialize() {

		if(inventory.count()>0) {
			return;
		}

		inventory.save(new InventoryItem(productCatalog.findByName("Salami").get().findFirst().get(),Quantity.of(1)));
		inventory.save(new InventoryItem(productCatalog.findByName("Ham").get().findFirst().get(),Quantity.of(2)));

		productCatalog.findAll().forEach(item -> {
			if(!inventory.findByProduct(item).isPresent()){ inventory.save(new InventoryItem(item, Quantity.of(100)));}
		});
	}

}
