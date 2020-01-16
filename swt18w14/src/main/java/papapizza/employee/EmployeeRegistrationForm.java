package papapizza.employee;

import javax.validation.constraints.NotEmpty;

/**
 * The form for creating employees.
 * @author Toni Tanneberger
 */
interface EmployeeRegistrationForm {

	@NotEmpty(message = "{RegistrationForm.firstname.NotEmpty}")
	String getFirstname();

	@NotEmpty(message = "{RegistrationForm.lastname.NotEmpty}")
	String getLastname();


	@NotEmpty(message = "{RegistrationForm.password.NotEmpty}")
	String getPassword();

	@NotEmpty(message = "{RegistrationForm.address.NotEmpty}")
	String getAddress();

	@NotEmpty(message = "{RegistrationForm.number.NotEmpty}")
	String getNumber();

	@NotEmpty(message = "{RegistrationForm.type.NotEmpty}")
	String getType();

	@NotEmpty(message = "{RegistrationForm.username.NotEmpty}")
	String getUsername();
}
