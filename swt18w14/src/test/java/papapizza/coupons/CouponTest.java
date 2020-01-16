package papapizza.coupons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;


import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@WebAppConfiguration
public class CouponTest {



	@BeforeEach
	public void Init(){
		Coupon defaultCoupon = new Coupon();
		Coupon onlyCodeCoupon = new Coupon("FOR_GONDOR", false	);
		Coupon onlyDescription = new Coupon("THIS IS PAPAPIIIIIZZZZZAAAA", true);
		Coupon bothCodeDesc	 = new Coupon("For_The_Watch", "Allahu Akbar");
	}

	@Test
	public void testCorrectCode(){

		Coupon onlyCodeCoupon = new Coupon("FOR_GONDOR", false	);
		assertEquals("FOR_GONDOR", onlyCodeCoupon.getCode() );
		Coupon bothCodeDesc	 = new Coupon("For_The_Watch", "NightWatch");
		assertEquals("NightWatch", bothCodeDesc.getCode());
	}

	@Test
	public void getCorrectDescription(){

		Coupon defaultCoupon = new Coupon();
		assertEquals("Join the dark side of the force, we got the best pizza in the universe",
				defaultCoupon.getDescription());
		Coupon onlyCodeCoupon = new Coupon("FOR_GONDOR", true	);
		assertEquals("FOR_GONDOR", onlyCodeCoupon.getDescription() );
		Coupon bothCodeDesc	 = new Coupon("For_The_Watch", "NightWatch");
		assertEquals("For_The_Watch", bothCodeDesc.getDescription());
	}


	@Test
	public void CompareStringsWithSpacing(){
		Coupon defaultCode	 = new Coupon();
		String s = defaultCode.getCode();
		assertTrue(defaultCode.tryToFindAFittingCode(s));
	}


	@Test
	public void CompareStringsWithOutSpacing(){
		Coupon defaultCode	 = new Coupon();
		String s = defaultCode.getCode();
		s = s.replaceAll("//s", "");
		assertTrue(defaultCode.tryToFindAFittingCode(s));
	}

	@Test
	public void TestTime(){
		Coupon defaultCode	 = new Coupon();
		LocalDate localDate = LocalDate.now();
		assertEquals(localDate.toString(), defaultCode.getDate());
	}

	@Test
	public void TestSettingDescriptionDefaut(){
		Coupon defaultCode	 = new Coupon();
		String s = "halloWelt";
		defaultCode.setDescription(s);
		assertEquals(s, defaultCode.getDescription());
	}

	@Test
	public void TestSettingDescriptionNotDefault(){
		Coupon defaultCode	 = new Coupon("helloWorld", true);
		String s = "halloWelt";
		defaultCode.setDescription(s);
		assertEquals(s, defaultCode.getDescription());
	}

	@Test
	public void TestSettingCodeDefaut(){
		Coupon defaultCode	 = new Coupon();
		String s = "halloWelt";
		defaultCode.setCouponCode(s);
		assertEquals(s, defaultCode.getCode());
	}

	@Test
	public void TestSettingCodeNotDefault(){
		Coupon defaultCode	 = new Coupon("helloWorld", false);
		String s = "halloWelt";
		defaultCode.setCouponCode(s);
		assertEquals(s, defaultCode.getCode());
	}
}
