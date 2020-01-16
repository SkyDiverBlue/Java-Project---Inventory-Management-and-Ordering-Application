package papapizza.coupons;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@SessionAttributes("couponsRepo")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class CouponController {

	//@Autowired
	private final CouponsRepo couponsRepo;

    @Autowired
	public CouponController (CouponsRepo couponsRepo){
		this.couponsRepo = couponsRepo;
		CouponInit couponInit = new CouponInit(this.couponsRepo, this);
		couponInit.initialize();
	}

	/**
	 * returns a list of all stored Coupons to the html
	 * @param model
	 * @return link
	 */
	@RequestMapping(value = "coupons", method = RequestMethod.GET)
	public String showCoupons(Model model){

		model.addAttribute("allCoupons", couponsRepo.findAll());

		return "coupons";
	}


	/**
	 * Removes a code from the Repo
	 * @return link
	 */
	@GetMapping("coupons/removeCoupon/{id}")
	public String deleteCoupon(@PathVariable long id){
		try {
			couponsRepo.deleteById(id);
		} catch (Exception e){
			System.out.println("schade");
		}
		return "redirect:/coupons";
	}

	/**
	 * generates a new Coupon and stores it
	 * @param code
	 * @param description
	 * @return
	 */
	@RequestMapping(value = "coupons/generateNewCoupon", method = RequestMethod.POST)
	public String generateNewCoupon(@RequestParam("code") String code, @RequestParam("description") String description){
    	System.out.println(code + "  " + description);

    	if (description.equals("") && code.equals("")){
			couponsRepo.save(new Coupon());
		} else if(description.equals("") && !code.equals("")) {
			couponsRepo.save(new Coupon(code, false));
		} else if(!description.equals("") && code.equals("")){
    		couponsRepo.save(new Coupon(description, true));
		} else {
    		couponsRepo.save(new Coupon(description, code));
		}
    	System.out.println(couponsRepo.count());


    	return "redirect:/coupons";
	}


}
