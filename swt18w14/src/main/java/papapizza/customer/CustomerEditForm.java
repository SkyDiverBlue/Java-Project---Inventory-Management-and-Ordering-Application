package papapizza.customer;

import javax.validation.constraints.*;

interface CustomerEditForm {

	@NotBlank(message = "The firstname must not be empty.") //
	@Size(max=30)
	String getFirstname();

	@NotBlank(message = "The lastname must not be empty.") //
	@Size(max=30)
	String getLastname();

	@NotBlank(message = "The tel-number must not be empty.") //
	@Size(max=15)
	String getTel();

	@NotBlank(message = "The address must not be empty.") //
	@Size
	String getAddress();

	@NotNull(message = "The returned DiningSets must not be empty.") //
	@Min(0)
	@Max(1000)
	long getReturnDiningSet();

}
