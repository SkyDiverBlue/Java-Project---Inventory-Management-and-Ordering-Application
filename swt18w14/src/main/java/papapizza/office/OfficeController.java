package papapizza.office;



import org.javamoney.moneta.Money;
import org.salespointframework.accountancy.Accountancy;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.time.Interval;
import org.springframework.data.util.Streamable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import javax.validation.Valid;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;


import static org.salespointframework.core.Currencies.EURO;


@Controller
@SessionAttributes("accountancy")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class OfficeController  {

	private final Accountancy accountancy;
	private final BusinessTime businessTime;
	private final Office office;

	public OfficeController(Accountancy accountancy, BusinessTime businessTime, Office office){

		Assert.notNull(accountancy, "The Accountancy must not be null!");
		Assert.notNull(businessTime, "The BusinessTime must not be null!");
		Assert.notNull(office, "The Office must not be null!");

		this.accountancy = accountancy;
		this.businessTime = businessTime;
		this.office = office;
		businessTime.reset();

		OfficeInitializer o = new OfficeInitializer(accountancy, this, businessTime);
		o.initialize();
	}





	@RequestMapping(value="/office", method= RequestMethod.GET)
	public String wasPaid(Model model) {
		Assert.notNull(model, "model should not be null");


		model.addAttribute("date", LocalDateTime.now().minusDays(34));
		model.addAttribute("lastDate", LocalDateTime.now().plusDays(8));
		model.addAttribute("accountings", accountancy.findAll());
		model.addAttribute("accountancy", this);

		model.addAttribute("format", DateTimeFormatter.ofPattern("dd.MM.yyyy"));


		return "office";

	}

	@GetMapping("/changeInterval")
	public String changeInterval(@RequestParam("start") String start, @RequestParam("end") String end, Model model) {

		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate startDate = LocalDate.parse(start, dateFormat).minusDays(1);
		LocalDate endDate = LocalDate.parse(end, dateFormat);
		System.out.println(startDate + "  " + endDate);

		model.addAttribute("accountings", accountancy.find(Interval.from(LocalDateTime.of(startDate,
				LocalTime.now())).to(LocalDateTime.of(endDate, LocalTime.now()))));
		model.addAttribute("accountancy", this);

		model.addAttribute("format", DateTimeFormatter.ofPattern("dd.MM.yyyy"));


		return "office";
	}



	public Money sumValue(Streamable<AccountancyEntry> stream) {

		Assert.notNull(stream, "stream should not be null");

		Money amount = Money.of(0, "EUR");
		for (AccountancyEntry accountancyEntry: stream){
			amount = amount.add(accountancyEntry.getValue());
		}

		return amount;
	}


	@GetMapping("/getQuartal")
	public String getQuartal(Model model){
		int dayOfYear = LocalDate.now().getDayOfYear();
		int moveForward = 0;
		LocalDate start =  LocalDate.now();

		if (LocalDate.now().getDayOfYear()> 270){
			 moveForward = dayOfYear - 270;
			 start = LocalDate.now().minusDays(moveForward);
		} else if (LocalDate.now().getDayOfYear()> 180){
			moveForward = dayOfYear - 180;
			start = LocalDate.now().minusDays(moveForward);
		} else if (LocalDate.now().getDayOfYear()> 90){
			moveForward = dayOfYear - 90;
			start = LocalDate.now().minusDays(moveForward);
		} else{
			start = LocalDate.now().minusDays(LocalDate.now().getDayOfYear());
		}

		model = accountancyModel(model, start);

		model.addAttribute("format", DateTimeFormatter.ofPattern("dd.MM.yyyy"));

		return "office";
	}

	@GetMapping("/getWeeklyNews")
	public String getWeeklyNews(Model model){

		LocalDate t = LocalDate.now();
		int days = t.getDayOfWeek().getValue();

		LocalDate start = LocalDate.now().minusDays(days);

		model = accountancyModel(model,start);

		model.addAttribute("format", DateTimeFormatter.ofPattern("dd.MM.yyyy"));


		return "office";
	}

	@GetMapping("/addAccountancyEntry")
	public String addAccountancyEntry(@RequestParam("value") float value, @RequestParam("description") String description,
									  @RequestParam("date") String date) {
		businessTime.reset();

		Assert.notNull(value, "value should not be null");
		Assert.notNull(date, "date should not be null");
		Assert.notNull(description, "description should not be null");
		Assert.hasText(description, "A description must be included");


		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate startDate = LocalDate.parse(date, dateFormat);


		long yearStart = startDate.getYear();
		long yearNow = LocalDate.now().getYear();
		long year = yearStart - yearNow;


		long totalDays = 0;
		System.out.println(startDate.getYear() +  "  " + LocalDate.now().getYear() + " " + year);
		if (year < 0){
			totalDays = -getTotalDays(startDate.getYear(),LocalDate.now().getYear());
		}
		else if (year > 0){
			totalDays = getTotalDays(LocalDate.now().getYear(),startDate.getYear());
		}
		totalDays += startDate.getDayOfYear() - LocalDate.now().getDayOfYear();

		businessTime.forward(Duration.ofDays(totalDays));

		accountancy.add(new AccountancyEntry(Money.of(value, EURO), description));
		businessTime.reset();
		return "redirect:/office";
	}


	private long getTotalDays(int start, int end){
		long totalDays = 0;
		for (int i = start; i < end; i++){
			if (Year.of(i).isLeap()){
				totalDays += 366;
			}
			else {
				totalDays += 365;
			}
		}
		return totalDays;
	}

	@GetMapping ("/oven")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public String oven(Model model) {

		model.addAttribute("inUsage", office.getOvensInUsage());
		model.addAttribute("timeAverage", office.getWaitingtime());
		model.addAttribute("oven", office.getOven());


		int utilization = office.getUtilization();
		String message;

		if(utilization<0){
			message = "Es gibt " + utilization*(-1) + " unbediente Öfen.";
		} else if(utilization>0){
			message = "Es gibt " + utilization + " Bäcker ohne Ofen.";
		} else {
			message = "Alle Öfen werden durch Bäcker bedient.";
		}

		model.addAttribute("utilization", message);
		return "oven";
	}

	@PostMapping ("/oven")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	String editovens(@Valid OvenForm form, Errors result, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {

			redirectAttributes.addFlashAttribute("error", true);
			return "redirect:/oven";
		}

		office.setOven(form.getOvens());

		redirectAttributes.addFlashAttribute("error", false);

		return "redirect:/oven";
	}

	private Model accountancyModel(Model model, LocalDate start){
		Streamable temp = accountancy.find(Interval.from(LocalDateTime.of(start,
				LocalTime.now())).to(LocalDateTime.of(LocalDate.now(), LocalTime.now())));
		model.addAttribute("accountings", temp);
		model.addAttribute("accountancy", this);

		return model;
	}

}