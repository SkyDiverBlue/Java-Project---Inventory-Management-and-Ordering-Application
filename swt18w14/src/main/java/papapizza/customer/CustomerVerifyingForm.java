package papapizza.customer;

import javax.validation.constraints.NotBlank;

interface CustomerVerifyingForm {

	@NotBlank(message = "The tel-number must not be empty.") //
	String getTel();

	@NotBlank(message = "The TAN must not be empty.") //
	String getTan();

}
