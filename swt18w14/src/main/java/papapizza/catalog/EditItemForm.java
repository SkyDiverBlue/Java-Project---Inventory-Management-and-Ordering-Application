package papapizza.catalog;

import javax.validation.constraints.NotBlank;

/**
 * This class manages and tests the form input for editing an "Item" in the database(ProductCatalog).
 * @author Moritz Voigt
 */
interface EditItemForm {
	@NotBlank
	String getName();

	@NotBlank
	String getPrice();

	@NotBlank
	String getPurchasingPrice();

	@NotBlank
	String getImage();

	@NotBlank
	String getDescription();
}
