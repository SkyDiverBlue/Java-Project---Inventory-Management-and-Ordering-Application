package papapizza.catalog;


import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.quantity.Metric;
import org.salespointframework.quantity.Quantity;

import javax.money.MonetaryAmount;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;


import static org.salespointframework.core.Currencies.EURO;


@Entity
@Table(name = "pizza")
public class PizzaProduct extends Item implements Serializable {

	@Lob
	@Column(name = "toppings", length = 10000)
	private TreeMap<Product, Integer> toppings = new TreeMap<>();
	private String name;
	private Metric metric;
	private ProductType productType;
	private LocalDateTime localDateTime;
	private String relatedOrder ="";


	protected PizzaProduct(){}

	public PizzaProduct(String Name, Metric metric, ProductType productType){
		super(Name, metric, Money.of(0, EURO), ProductType.PIZZA);
		//super(Name, Money.of(0, EURO), metric);
		this.name = Name;
		this.metric = metric;
		this.productType = productType;
		this.localDateTime = LocalDateTime.now();
	}


	public TreeMap<Product, Integer> getToppings(){
		return toppings;
	}

	public void addTopping(Product product, Quantity quantity){
		if (toppings.containsKey(product)) {
			int q = toppings.get(product);
			Quantity c = Quantity.of(q).add(quantity);
			toppings.replace(product, c.getAmount().intValue());
		} else {
			toppings.put(product, quantity.getAmount().intValue());
		}
	}


	public void decreaseProductAmmount(Product product, Quantity quantity){
		if (toppings.containsKey(product)) {
			System.out.println("warum bin ich hier " + toppings.get(product));
			toppings.replace(product, toppings.get(product)-quantity.getAmount().intValue());
			System.out.println("warum bin ich hier" + toppings.get(product));
			if (toppings.get(product)<1) {
				toppings.remove(product);
				System.out.println("Mhhhh warum bin ich nicht hier");
			}
		}
	}

	public void removeProduct(Product product){

		if (toppings.containsKey(product)) {
			System.out.println("Produkt wurde entfernt");
			toppings.remove(product);
		}
	}


	public Quantity getQuantityOfProduct(Product product){
		if (toppings.containsKey(product)) {
			return Quantity.of(toppings.get(product));
		} else {
			return null;
		}
	}



	public MonetaryAmount getPrice(){
		MonetaryAmount money = Money.of(0, EURO);
		for (Map.Entry<Product, Integer> sumUP : toppings.entrySet()) {
			money = money.add(sumUP.getKey().getPrice().multiply(sumUP.getValue()));
		}
		return money;
	}

	public LocalDateTime getTime(){
		return this.localDateTime;
	}

	public void setRelatedOrder(String s){
		this.relatedOrder = s;
	}

}
