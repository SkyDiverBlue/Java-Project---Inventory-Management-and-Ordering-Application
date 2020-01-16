package papapizza.coupons;

import org.salespointframework.core.DataInitializer;

class CouponInit implements DataInitializer {

	private final CouponsRepo couponsRepo;
	private final CouponController couponController;

	public CouponInit(CouponsRepo couponsRepo, CouponController couponController){
		this.couponsRepo = couponsRepo;
		this.couponController = couponController;
	}

	@Override
	public void initialize() {
		if (couponsRepo.count() == 0) {
			Coupon a = new Coupon("this has been the best discount code in the history of discount codes, maybe ever", true);
			couponsRepo.save(a);

			Coupon b = new Coupon("THESE DISCOUNTS ARE AMAZING", true);
			couponsRepo.save(b);

			Coupon c = new Coupon("cause i wrote it on my own", "seemsToBeAShittyCode");
			couponsRepo.save(c);

			Coupon d = new Coupon("SPARTANS! Prepare for Pizza! For tonight, WE DINE AT PAPPAPIZZA!!", true);
			couponsRepo.save(d);

			Coupon e = new Coupon("I'm going to make you an offer you can't refuse.", true);
			couponsRepo.save(e);

			Coupon f = new Coupon("It came to me. My own. My love. My own. My pizza", true);
			couponsRepo.save(f);
			
			Coupon g = new Coupon("Pizza time", "pizzaAllDay");
			couponsRepo.save(g);
		}
	}
}
