package papapizza.customer;

import javax.validation.constraints.NotBlank;

interface CustomerRegistrationForm {

	@NotBlank(message = "The firstname must not be empty.") //
	String getFirstname();

	@NotBlank(message = "The lastname must not be empty.") //
	String getLastname();

	@NotBlank(message = "The tel-number must not be empty.") //
	String getTel();

	@NotBlank(message = "The address must not be empty.") //
	String getAddress();
}
