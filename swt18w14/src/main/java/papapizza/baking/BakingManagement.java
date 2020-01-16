package papapizza.baking;

import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.UserAccountIdentifier;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import papapizza.bill.OrderRepo;

import papapizza.catalog.PizzaProduct;
import papapizza.order.CustomerOrder;
import papapizza.order.KindOfPizza;


import javax.tools.Diagnostic;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Component
public class BakingManagement {


	private final BusinessTime businessTime;
	private final OrderRepo orderRepo;
	private final PizzaRepo pizzaRepo;


	BakingManagement(BusinessTime businessTime, OrderRepo orderRepo, PizzaRepo pizzaRepo) {

		Assert.notNull(businessTime, "Businesstime must not be null!");
		Assert.notNull(orderRepo, "Order-Repository must not be null!");
		Assert.notNull(pizzaRepo, "PizzaRepos must not be null!");

		this.businessTime = businessTime;
		this.pizzaRepo = pizzaRepo;
		this.orderRepo = orderRepo;
	}

	/**
	 * Every 5 seconds the application checks which pizzas are supposed to be ready
	 */
	@Scheduled(fixedRate = 5000)
	public void inOvenUpdate() {

		for (KindOfPizza baking : pizzaRepo.findAll()) {
			if(!baking.getUserAcId().equals("")) {
				if (baking.getTime().isBefore(businessTime.getTime())) {

					for (CustomerOrder customerOrder : orderRepo.findAll()) {
						if (customerOrder.getId().getIdentifier().equals(baking.getRelatedOrder())) {
							customerOrder.decreaseAmount();
							if (customerOrder.getAmountOfPizzen() == 0) {
								customerOrder.isNowCompleted();
							}
							orderRepo.save(customerOrder);
						}
					}
					pizzaRepo.delete(baking);
				}
			}
		}
	}

	public KindOfPizza nextPizza() {

		if (toBeBakedAmount() == 0) {
			return null;
		}

		KindOfPizza waitedLong = null;
		LocalDateTime time = LocalDateTime.MAX;

		for (KindOfPizza pizza : pizzaRepo.findAll()) {

			if (pizza.getTime().isBefore(time) && pizza.getUserAcId().equals("")) {
				waitedLong = pizza;
				time = pizza.getTime();
			}
		}
		return waitedLong;
	}

	public KindOfPizza findPizzaByUUID(String uuid) {

		for (KindOfPizza pizza : getToBeBaked()) {
			if (pizza.getUUID().equals(uuid) && pizza.getUserAcId().equals("")) {
				return pizza;
			}
		}
		return null;
	}

	public void putInOven(UserAccountIdentifier baker, KindOfPizza kindOfPizza) {


		kindOfPizza.setTime(businessTime.getTime().plusMinutes(5));  ///plusMinutes(5)
		kindOfPizza.setUserAcId(baker.getIdentifier());
		pizzaRepo.save(kindOfPizza);
	}

	public Model bakingBaker(UserAccountIdentifier id, Model model) {

		for (KindOfPizza baking : pizzaRepo.findAll()) {
			if (baking.getUserAcId().equals(id.getIdentifier())) {
				model.addAttribute("nextPizza", baking);
				model.addAttribute("alreadyBaking", true);
			}
		}
		return model;
	}


	public ArrayList<KindOfPizza> getToBeBaked() {

		ArrayList<KindOfPizza> kindOfPizzas = new ArrayList<>();
		for (KindOfPizza kindOfPizza: pizzaRepo.findAll()){
			if (kindOfPizza.getUserAcId().equals("")){
				kindOfPizzas.add(kindOfPizza);
			}
		}
		return kindOfPizzas;
	}


	public void addToPizzaRepo(KindOfPizza p) { pizzaRepo.save(p); }

	public ArrayList<KindOfPizza> getInOven() {
		ArrayList<KindOfPizza> kindOfPizzas = new ArrayList<>();
		for (KindOfPizza kindOfPizza: pizzaRepo.findAll()){
			if (!kindOfPizza.getUserAcId().equals("")){
				kindOfPizzas.add(kindOfPizza);
			}
		}

		return kindOfPizzas;
	}

	public long inOvenAmount() {
		return getInOven().size();
	}

	public long toBeBakedAmount() {
		return getToBeBaked().size();
	}

	public void clearPizzaRepo() {
		pizzaRepo.deleteAll();
	}


}

