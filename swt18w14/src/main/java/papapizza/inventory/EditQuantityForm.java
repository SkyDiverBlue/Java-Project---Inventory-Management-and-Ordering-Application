package papapizza.inventory;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

interface EditQuantityForm {
	@NotBlank(message = "The Quantity must not be null.") //
	@Min(value = 0, message = "amount of a product must not be less than 0.") //
	String getQuantity();

}