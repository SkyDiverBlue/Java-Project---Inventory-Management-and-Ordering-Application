package papapizza.office;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

interface OvenForm {

	@NotNull(message = "The count of physical ovens must not be empty.") //
	@Min(value=0, message = "The count of physical ovens must be at least zero.") //
	@Max(1000)
	long getOvens();

}