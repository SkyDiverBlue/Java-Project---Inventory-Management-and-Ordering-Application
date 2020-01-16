package papapizza.catalog;

import static org.salespointframework.core.Currencies.*;

import javax.persistence.*;


import org.javamoney.moneta.Money;
import org.salespointframework.quantity.Metric;
import org.salespointframework.quantity.Quantity;
import org.springframework.lang.NonNull;

import java.io.Serializable;


/**
 *  An Item is either a possible "topping" for a pizza, or another product, like "beverage" or "salad".
 *  <br>This class creates an Item that can be saved in a database (ProductCatalog). An Item contains
 *  Information about the item name, selling- and purchasing price, a description, an image path and
 *  what kind of Item it is.
 * @author Moritz Voigt
 */


@Entity
@Table(name = "item")
public class Item extends org.salespointframework.catalog.Product implements Serializable{


	private String image;
	private String description;
	private ProductType type;
	private Money purchasingPrice;


	Item() {}

	public Item(String name, String image,String description, Money sellingPrice, Money purchasingPrice,ProductType type) {
		super(name, sellingPrice);

		if(sellingPrice.isNegative() || purchasingPrice.isNegative()) {
			throw new IllegalArgumentException("price cannot be negative");
		} else {
			this.image = image;
			this.description = description;
			this.purchasingPrice = purchasingPrice;
			this.type = type;
		}
	}

	public Item (String name, Metric metric, Money money, ProductType type){
		super(name, money,metric);
		this.type = type;
		this.description  = "Just a new Pizza, nothing to worry about";
		this.purchasingPrice = Money.of(0, EURO);
		this.image = "";
	}

	public void setPurchasingPrice(@NonNull Money purchasingPrice){
		this.purchasingPrice = purchasingPrice;
	}

	@NonNull
	public Money getPurchasingPrice(){
		return purchasingPrice;
	}

	public ProductType getType(){
		return type;
	}

	public void setType(ProductType type) {
		this.type = type;
	}

	public String getDescription() {return description;}

	public void setDescription(String description){this.description=description;}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String germanProductType(){
		if(getType().equals(ProductType.BEVERAGE)){
			return "Getränk";
		}
		if(getType().equals(ProductType.TOPPING)){
			return "Belag";
		}
		if(getType().equals(ProductType.SALAD)){
			return "Salat";
		}
		else{
			return  "Garnitur";
		}
	}


}