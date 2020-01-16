package papapizza.office;

import org.salespointframework.time.BusinessTime;
import org.salespointframework.time.Interval;
import org.salespointframework.useraccount.UserAccountIdentifier;
import org.springframework.data.util.Pair;
import org.springframework.data.util.Streamable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import papapizza.baking.BakingManagement;
import papapizza.bill.OrderRepo;
import papapizza.employee.EmployeeManagement;
import papapizza.catalog.PizzaProduct;
import papapizza.order.CustomerOrder;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Component
public class Office{

	private final EmployeeManagement employeeManagement;
	private final BusinessTime businessTime;
	private final OrderRepo orderRepo;
	private LocalDateTime deliveryTime;
	private long oven;
	private final ConfigController configController;
	private long criticalAmount; //criticalAmount for low on stock warning



	Office (EmployeeManagement employeeManagement, BusinessTime businessTime, OrderRepo orderRepo){
		this.employeeManagement = employeeManagement;
		this.businessTime = businessTime;
		this.orderRepo = orderRepo;
		deliveryTime = businessTime.getTime().plusMinutes(15);
		this.configController = new ConfigController();

		System.out.println(configController.readFile("OvenAmount"));
		System.out.println(configController.readFile("CriticalAmount"));
		oven = Long.valueOf(configController.readFile("OvenAmount"));
		criticalAmount = Long.valueOf(configController.readFile("CriticalAmount"));
	}

	@Scheduled(cron = "0 0/15 * * * ?")
	void updateDeliveryTime(){
		deliveryTime = businessTime.getTime().plusMinutes(15);
	}

	public LocalDateTime getDeliveryTime() { return deliveryTime; }

	public long includeDeliveryTime(long x) {

		LocalDateTime now = businessTime.getTime();
		LocalDateTime whichDelivery = deliveryTime.plusMinutes((x/15)*15);
		Duration difference = Duration.between(now, whichDelivery);

		return difference.toMinutes();
	}

	long getWaitingtime() {

		long time = 0;

		for (CustomerOrder order : orderRepo.findAll()){
			time = time + order.getGuessedDeliveryTime();
		}

		long count = 1;
		if (orderRepo.count() == 0){
			count = 1;
		}
		else {
			 count = orderRepo.count();
		}
		time = time/count;

		return time;
	}

	public long getOven() { return oven; }

	public void setOven(long oven) {
		configController.overWriteConfigFile("OvenAmount = " , String.valueOf(oven));
		this.oven = oven;
	}

	public long getOvensInUsage(){
		return Math.min(oven, employeeManagement.countBakers());
	}

	public int getUtilization() { return (int) employeeManagement.countBakers() - (int) oven; }

	public long getCriticalAmount() {
		return criticalAmount;
	}

	public void setCriticalAmount(long criticalAmount) {
		configController.overWriteConfigFile("CriticalAmount = " , String.valueOf(criticalAmount));
		this.criticalAmount = criticalAmount;
	}

}