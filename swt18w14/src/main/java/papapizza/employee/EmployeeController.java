package papapizza.employee;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;

/**
 * The Controller class which maps every action involving employee management.
 * @author Toni Tanneberger
 */
@Controller
public class EmployeeController {

	private final EmployeeManagement employeeManagement;

	EmployeeController(EmployeeManagement employeeManagement) {

		Assert.notNull(employeeManagement, "userManagement must not be null!");

		this.employeeManagement = employeeManagement;
	}

	/**
	 * A new employee is created if there were no errors.
	 * @param form - filled in Employee registration form.
	 * @param result - contains errors
	 * @return redirects
	 */
	@PostMapping("/registerEmployee")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	String registerNew(@Valid EmployeeRegistrationForm form, Errors result, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			System.out.println("ERRORR");
			redirectAttributes.addFlashAttribute("registered" , false);
			return "redirect:/employees";
		}
		try {
			employeeManagement.createEmployee(form);
		} catch (IllegalArgumentException a) {
			redirectAttributes.addFlashAttribute("registered", false);
			redirectAttributes.addFlashAttribute("nameExists" ,true);
			return "redirect:/employees";
		}

		redirectAttributes.addFlashAttribute("registered", true);
		return "redirect:/employees";
	}

	/**
	 * Maps the page for employee registration.
	 * @param model
	 * @param form
	 * @return redirects
	 */
	@GetMapping("/registerEmployee")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	String register(Model model, EmployeeRegistrationForm form) {
		model.addAttribute("form", form);
		return "registerEmployee";
	}

	/**
	 * Maps the page to view all employees
	 * @param model
	 * @return redirects
	 */
	@GetMapping("/employees")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	String employees(Model model) {

		model.addAttribute("employeeList", employeeManagement.findAllEmployees());

		return "employees";
	}

	/**
	 * Edits the information of a selected employee if there were no errors.
	 * @param id  the unique identifier of the employee who will be edited
	 * @param form
	 * @param result
	 * @return redirects
	 */
	@PostMapping("/employee/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	String editnew(@PathVariable long id, @Valid EmployeeEditForm form, Errors result,
				   RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			System.out.println(result);
			redirectAttributes.addFlashAttribute("edit", false);
			return "redirect:/employees";
		}

		employeeManagement.editEmployee(form, id);
		redirectAttributes.addFlashAttribute("edit", true);

		return "redirect:/employees";
	}

	/**
	 * Maps the page where a selected employee can be edited.
	 * @param id the unique identifier of the employee who will be edited
	 * @param model
	 * @param form
	 * @return redirects
	 */
	@GetMapping("/employee/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	String edit(@PathVariable long id, Model model, EmployeeEditForm form) {

		model.addAttribute("employee", employeeManagement.findEmployee(id));
		model.addAttribute("form", form);

		return "editEmployee";
	}

	/**
	 * Deletes the selected employee.
	 * @param id the unique identifier of the employee who will be deleted
	 * @return
	 */
	@GetMapping("/employee/{id}/delete")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	String delete(@PathVariable long id, RedirectAttributes redirectAttributes) {
		employeeManagement.delete(id);
		redirectAttributes.addFlashAttribute("deleted", true);
		return "redirect:/employees";
	}

	@GetMapping("/searche")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	String search(@RequestParam("search") String search, Model model) {

		ArrayList<Employee> employees = new ArrayList<>();
		boolean res=false;

		if(search.equals("")) {
			model.addAttribute("employeeList", employeeManagement.findAllEmployees());
		} else{
			employeeManagement.findAllEmployees().forEach(employee -> {
				if(getFullName(employee).toLowerCase().contains(search.toLowerCase())){
					employees.add(employee);
				}
			});
			res=employees.isEmpty();
			if(res) {
				model.addAttribute("employeeList", employeeManagement.findAllEmployees());
			} else {
				model.addAttribute("employeeList", employees);
			}
		}
		model.addAttribute("noresult",res);
		return "employees";
	}

	public String getFullName(Employee employee){return employee.getUseraccount().getFirstname() + " "
			+ employee.getUseraccount().getLastname();
	}

}
