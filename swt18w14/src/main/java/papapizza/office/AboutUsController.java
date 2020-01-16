package papapizza.office;



import net.bytebuddy.implementation.ToStringMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AboutUsController {

	private ConfigController configController;

	@Autowired
	protected AboutUsController (){
		this.configController = new ConfigController();
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("aboutUsEdit")
	public String nothing(Model model){
		addAll(model);
		return "aboutUsEdit";
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/aboutUsEdit/changeNumber", method= RequestMethod.GET)
	public String saveTelephonNumber(@RequestParam("tel") String tel, Model model, RedirectAttributes redirectAttributes){
		if (tel.length()> 6 && tel.length()<16) {
			configController.overWriteConfigFile("TELEPHONNUMBER = ", tel);
		}
		else redirectAttributes.addFlashAttribute("error", "Fehler bei eingabe der Telefonnummer");
		addAll(model);
		return "redirect:/aboutUsEdit";
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "aboutUsEdit/changeAdress", method = RequestMethod.GET)
	public String editAddres(@RequestParam("name") String name, @RequestParam("street") String street,
							 @RequestParam("postCode") String postCode, @RequestParam("city") String city, Model model){
		configController.overWriteConfigFile("NAME = ", name);
		configController.overWriteConfigFile("STRASSE = ", street);
		configController.overWriteConfigFile("POSTCODE = ", postCode);
		configController.overWriteConfigFile("CITY = ", city);
		addAll(model);

		return "redirect:/aboutUsEdit";
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "aboutUsEdit/information", method = RequestMethod.GET)
	public String informationChange(@RequestParam("information") String info, Model model){

		configController.overWriteConfigFile("INFORMATIONEN = ", info);
		addAll(model);

		return "redirect:/aboutUsEdit";
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "aboutUsEdit/email", method = RequestMethod.GET)
	public String email(@RequestParam("email") String info, Model model){

		configController.overWriteConfigFile("EMAIL = ", info);
		addAll(model);

		return "redirect:/aboutUsEdit";
	}
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "aboutUsEdit/weAreWorkingWeek", method = RequestMethod.GET)
	public String workTimes(@RequestParam("DayOn") String start, @RequestParam("DayOf") String stop,Model model){
		addToConfig(start, stop, "week");
		addAll(model);
		return "redirect:/aboutUsEdit";
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "aboutUsEdit/weAreWorkingSat", method = RequestMethod.GET)
	public String workTimesSaturDay(@RequestParam("DayOn") String start, @RequestParam("DayOf") String stop,Model model){
		addToConfig( start, stop, "sat");
		addAll(model);
		return "redirect:/aboutUsEdit";
	}

	private void addToConfig(String start, String stop, String day){
		float beginn = Float.valueOf(start.replace(":", "."));
		float end = Float.valueOf(stop.replace(":", "."));
		if ( beginn > end) {
			if (day.equals("week")){
				configController.overWriteConfigFile("WOCHENTAGE = ", "geöffnet von: " + stop + " bis " + start);
			}
			else if (day.equals("sat")){
				configController.overWriteConfigFile("SAMSTAG = ", "geöffnet von: " + stop + " bis " + start);
			}
		}
		else{
			if (day.equals("week")){
				configController.overWriteConfigFile("WOCHENTAGE = ",  start + " bis " + stop);
			}
			else if (day.equals("sat")){
				configController.overWriteConfigFile("SAMSTAG = ", start + " bis " + stop);
			}
		}
	}

	@GetMapping(value = "/aboutUs")
	public String showUs(Model model){

		addAll(model);

		return "aboutUs";
	}

	private Model addAll(Model model){
		model.addAttribute("teleNum", configController.readFile("TELEPHONNUMBER = "));
		model.addAttribute("name", configController.readFile("NAME = "));
		model.addAttribute("street", configController.readFile("STRASSE = "));
		model.addAttribute("postCode", configController.readFile("POSTCODE = "));
		model.addAttribute("city", configController.readFile("CITY = "));
		model.addAttribute("info", configController.readFile("INFORMATIONEN = "));
		model.addAttribute("weekdays", configController.readFile("WOCHENTAGE = "));
		model.addAttribute("satDay", configController.readFile("SAMSTAG = "));
		model.addAttribute("email", configController.readFile("EMAIL = "));

		return model;
	}

}
