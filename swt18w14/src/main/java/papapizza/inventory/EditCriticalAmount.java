package papapizza.inventory;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

interface EditCriticalAmount {
	@NotBlank(message = "The CriticalAmount must not be null.") //
	@Min(value = 0, message = "CriticalAmount must not be less than 0.") //
	Long getCriticalAmount();
}
