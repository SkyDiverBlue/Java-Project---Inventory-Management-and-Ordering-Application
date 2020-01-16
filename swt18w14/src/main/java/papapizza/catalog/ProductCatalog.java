package papapizza.catalog;

import org.salespointframework.catalog.Catalog;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

public interface ProductCatalog extends Catalog<Item> {

	Sort DEFAULT_SORT = new Sort(Direction.DESC, "productIdentifier");

	Iterable<Item> findByType(ProductType type, Sort sort);

	default Iterable<Item> findByType(ProductType type) {
		return findByType(type, DEFAULT_SORT);
	}
}
