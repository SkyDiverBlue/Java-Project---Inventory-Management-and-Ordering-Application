package papapizza.office;

import org.assertj.core.error.ShouldBeEqualIgnoringMinutes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.accountancy.Accountancy;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import papapizza.employee.Employee;
import papapizza.employee.EmployeeManagement;
import papapizza.employee.EmployeeRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


@SpringBootTest
@WithMockUser(roles = "ADMIN")
class OfficeTest {

	@Autowired
	Office office;

	@Autowired
	EmployeeManagement management;

	@Autowired
	BusinessTime businessTime;

	@BeforeEach
	void setUp(){
		office.setOven(0);
	}

	@Test
	void getUtilisationNeg(){
		office.setOven( management.countBakers()+1);
		assertEquals(office.getUtilization(),-1);
		assertEquals(office.getOvensInUsage(), management.countBakers());
	}

	@Test
	void getUtilisationPos(){
		office.setOven(management.countBakers()-1);
		assertEquals(office.getUtilization(), 1);
	}

	@Test
	void getUtilisationZero(){
		office.setOven(management.countBakers());
		assertEquals(office.getUtilization(), 0);
	}

	@Test
	void updateDeliveryTime(){
		boolean a = false;
		office.updateDeliveryTime();
		LocalDateTime dT = office.getDeliveryTime().truncatedTo(ChronoUnit.SECONDS);
		LocalDateTime time = businessTime.getTime().plusMinutes(15).truncatedTo(ChronoUnit.SECONDS);
		if(dT.isEqual(time) || dT.isEqual(time.plusSeconds(1)) || dT.isEqual(time.minusSeconds(1))){
			a = true;
		}
		assertTrue(a);
	}

	@Test
	void includeDeliveryTime(){
		office.updateDeliveryTime();
		assertEquals(businessTime.getTime().plusMinutes(office.includeDeliveryTime(0)), office.getDeliveryTime());
	}

	@Test
	void getOven(){
		office.setOven(0);
		assertEquals(0, office.getOven());
	}

	@Test
	void criticalAmount(){
		office.setCriticalAmount(2);
		assertEquals(office.getCriticalAmount(), 2);
	}

}
