package papapizza.coupons;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WithMockUser(roles = "ADMIN")
public class CouponControllerTest {

	@Autowired
	private CouponsRepo couponsRepo;

	@Autowired
	private CouponController couponController;

	private Coupon couponDelete;

	@BeforeEach
	public void setUp(){
		couponDelete = new Coupon("just a coupon", "FreeStuff");
		couponsRepo.save(couponDelete);
		CouponController cc = new CouponController(couponsRepo); // I know I know, just for line coverage
	}

	@Test
	public void findByCode(){
		Coupon test = new Coupon("ABCDEFGHIJ", "WHYSOSERIOUS");
		couponsRepo.save(test);

		Coupon expect = new Coupon();
		for (Coupon coupon: couponsRepo.findAll()){
			if (coupon.tryToFindAFittingCode(test.getCode())) {
				expect = coupon;
			}
		}

		assertEquals(expect.getCode(),test.getCode());
	}

	@Test
	public void showCoupon(){
		Model model = new ExtendedModelMap();
		String returnedView = couponController.showCoupons(model);

		assertThat(returnedView).isEqualTo("coupons");
	}

	@Test
	public void deleteCoupon(){
		String returnedView = couponController.deleteCoupon(couponDelete.getId());

		assertFalse(couponsRepo.findById(couponDelete.getId()).isPresent());
		assertThat(returnedView).isEqualTo("redirect:/coupons");
	}

	@Test
	public void deleteCouponNonExistent(){
		String returnedView = couponController.deleteCoupon(9999);

		assertThat(returnedView).isEqualTo("redirect:/coupons");
	}

	@Test
	public void generateNewCoupon(){
		long couponAmount = couponsRepo.count();
		String description = "has description";
		String code = "code";
		String returnedView = couponController.generateNewCoupon(description, code);

		for(Coupon couponToFind: couponsRepo.findAll()){
			if(couponToFind.getDescription().equals(description)){
				assertEquals(code, couponToFind.getCode());
			}
		}
		assertEquals(couponAmount + 1, couponsRepo.count());
		assertThat(returnedView).isEqualTo("redirect:/coupons");
	}

	@Test
	public void generateNewCouponNoDescription(){
		long couponAmount = couponsRepo.count();
		String description = "";
		String code = "code";
		String returnedView = couponController.generateNewCoupon(code, description);

		for(Coupon couponToFind: couponsRepo.findAll()){
			if(couponToFind.getCode().equals(code)){
				assertEquals(false, couponToFind.isDescription());
			}
		}

		assertEquals(couponAmount + 1, couponsRepo.count());
		assertThat(returnedView).isEqualTo("redirect:/coupons");
	}

	@Test
	public void generateNewCouponNoCode(){
		long couponAmount = couponsRepo.count();
		String description = "cool description";
		String code = "";
		String returnedView = couponController.generateNewCoupon(code, description);

		for(Coupon couponToFind: couponsRepo.findAll()){
			if(couponToFind.getDescription().equals(description)){
				assertEquals(true, couponToFind.isDescription());
			}
		}

		assertEquals(couponAmount + 1, couponsRepo.count());
		assertThat(returnedView).isEqualTo("redirect:/coupons");
	}

	@Test
	public void generateNewCouponNoCodeOrDescription(){
		long couponAmount = couponsRepo.count();
		String description = "";
		String code = "";
		String returnedView = couponController.generateNewCoupon(code, description);

		assertEquals(couponAmount + 1, couponsRepo.count());
		assertThat(returnedView).isEqualTo("redirect:/coupons");
	}

}
