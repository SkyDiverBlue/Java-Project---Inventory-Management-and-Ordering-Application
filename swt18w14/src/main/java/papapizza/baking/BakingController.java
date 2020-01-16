package papapizza.baking;


import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountIdentifier;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.data.util.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import papapizza.bill.OrderRepo;
import papapizza.catalog.PizzaProduct;
import papapizza.office.Office;
import papapizza.order.KindOfPizza;

import java.util.Iterator;
import java.util.Optional;


@Controller
@SessionAttributes("accountancy")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BAKER')")
public class BakingController  {

	private final BakingManagement bakingManagement;
	private final Office office;

	public BakingController (BakingManagement bakingManagement, Office office){

		this.bakingManagement = bakingManagement;
		this.office = office;

		Assert.notNull(bakingManagement, "BakingManagement must not be null!");
		Assert.notNull(office, "Office must not be null!");
	}

	@GetMapping ("/toBeBaked")
	@PreAuthorize("hasRole('ROLE_BAKER')")
	public String toBeBaked (Model model) {

		model.addAttribute("toBeBaked", bakingManagement.getToBeBaked());

		return "toBeBaked";
	}

	@GetMapping ("/nextPizza")
	@PreAuthorize("hasRole('ROLE_BAKER')")
	public String nextPizza (Model model, @LoggedIn UserAccount userAccount) {

		UserAccountIdentifier id = userAccount.getId();

		model = bakingManagement.bakingBaker(id, model);

		if(model.containsAttribute("alreadyBaking")){

				return "nextPizza";
		}

		KindOfPizza pizza = bakingManagement.nextPizza();

		if(pizza != null) {
			model.addAttribute("nextPizza", pizza);
			model.addAttribute("button", true);
			model.addAttribute("alreadyBaking", false);

			if(bakingManagement.inOvenAmount() >= office.getOvensInUsage()) {
				model.addAttribute("noOven", "Es gibt derzeit keinen freien Ofen, der bedient werden könnte.");
				model.addAttribute("button", false);
			}

			return "nextPizza";
		} else {
			model.addAttribute("nextPizza", null);
			model.addAttribute("button", false);
			model.addAttribute("alreadyBaking", false);
			model.addAttribute("noPizza", "Derzeit sind keine Pizzen zu backen. Räumen Sie auf oder machen Sie eine Pause.");

			return "nextPizza";
		}

	}


	@GetMapping ("/nextPizza/{uuid}")
	@PreAuthorize("hasRole('ROLE_BAKER')")
	String nextPizza (@PathVariable String uuid, Model model, @LoggedIn UserAccount userAccount) {


		UserAccountIdentifier ua = userAccount.getId();

		for(KindOfPizza baking : bakingManagement.getInOven()){
			if(baking.getUserAcId().equals(ua.getIdentifier())) {
				model.addAttribute("nextPizza", baking);
				model.addAttribute("button", false);
				model.addAttribute("alreadyBaking", true);
				return "nextPizza";
			}
		}

		KindOfPizza pizza = bakingManagement.findPizzaByUUID(uuid);
		if (pizza == null || ua == null) { return "redirect:/"; }
		if(bakingManagement.inOvenAmount() >= office.getOvensInUsage()) {
			model.addAttribute("noOven", "Es gibt derzeit keinen freien Ofen, der bedient werden könnte.");
			model.addAttribute("button", true);
			model.addAttribute("alreadyBaking", false);

			return "nextPizza";
		}
		bakingManagement.putInOven(ua, pizza);

		model.addAttribute("nextPizza", pizza);
		model.addAttribute("button", false);
		model.addAttribute("alreadyBaking", false);

		return "nextPizza";
	}

	@GetMapping ("/inOven")
	@PreAuthorize("hasRole('ROLE_BAKER')")
	public String inOven (Model model) {

		bakingManagement.inOvenUpdate();

		model.addAttribute("inOven", bakingManagement.getInOven());

		return "inOven";
	}
}