package papapizza.catalog;

import javax.validation.constraints.NotBlank;

/**
 * This class manages and tests the form input for adding an "Item" to the database(ProductCatalog).
 * @author Moritz Voigt
 */
interface AddItemForm {
	@NotBlank
	String getName();
	@NotBlank
	String getDescription();
	@NotBlank
	String getPrice();
	@NotBlank
	String getPurchasingPrice();
	@NotBlank
	String getImage();

	ProductType getType();
	@NotBlank
	String getQuantity();
}
