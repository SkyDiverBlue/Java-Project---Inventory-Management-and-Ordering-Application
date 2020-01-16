package papapizza.employee;

import javax.validation.constraints.NotEmpty;

/**
 * The Form for editing employees.
 * @author Toni Tanneberger
 */
interface EmployeeEditForm {

	@NotEmpty(message = "Role must not be empty")
	String getType();

	@NotEmpty(message = "Firstname must not be empty")
	String getFirstname();

	@NotEmpty(message = "Lastname must not be empty")
	String getLastname();

	@NotEmpty(message = "Address must not be empty")
	String getAddress();

	@NotEmpty(message = "Phone number must not be empty")
	String getNumber();
}
