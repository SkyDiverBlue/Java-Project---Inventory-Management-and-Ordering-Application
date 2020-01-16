package papapizza.order;


import org.javamoney.moneta.Money;
import papapizza.customer.Customer;
import org.salespointframework.order.Order;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.persistence.*;


import org.springframework.util.Assert;

import static org.salespointframework.core.Currencies.EURO;

/**
 * This Class extends the order, adds more functionality
 */
@Entity
@Table(name = "customerOrder")
public class CustomerOrder extends Order {

	@OneToOne//
	@AttributeOverride(name = "id", column = @Column(name = "Product")) //
	private Customer customer;
	private LocalDateTime dateCreated = null;
	private long guessedDeliveryTime;
	private boolean deliver = true;
	private boolean completed = false;
	private boolean coupon = false;
	private Integer amountOfPizzen = 0;
	private Integer amountDining  = 0;

	protected  CustomerOrder(){}

	/**
	 * every CustomerOrder needs a Customer, sets Time
	 * @param customer
	 */
	CustomerOrder(Customer customer) {

		Assert.notNull(customer, "customer must not be null");

		this.customer = customer;
		this.dateCreated = LocalDateTime.now();
	}

	public void setDiningAmount(int dining){
		this.amountDining  = dining;
	}

	/**
	 * sets a coupon, to get a discount of 10%
	 * @param coupon
	 */
	public void setCoupon(boolean coupon) {
		this.coupon = coupon;
	}

	/**
	 * returns true if a coupon is used in this order
	 * @return
	 */
	public boolean isCoupon() {
		return coupon;
	}

	/**
	 * returns the Date
	 * @return
	 */
	public String getFormattedDateCreated() {
		return dateCreated.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + ", "
				+ dateCreated.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) + " Uhr";
	}

	/**
	 * every Order has a customer attached, this function return the customer
	 * @return
	 */
	public Customer getCustomer(){
		return customer;
	}

	/**
	 * returns the total price after all discounts
	 * @return
	 */
	@Override
	public MonetaryAmount getTotalPrice() {

		MonetaryAmount total = super.getTotalPrice();
		total = total.subtract(Money.of(amountDining*15, EURO));
		System.out.println(" diningAmount = " +  amountDining);

		if(deliver && !coupon) {
			return super.getTotalPrice() .with(Monetary.getDefaultRounding()).abs();
		} else if ((!deliver && !coupon) || (deliver && coupon)){
			return total.multiply(0.9).with(Monetary.getDefaultRounding()).add(Money.of(amountDining*15, EURO));

		} else {
			return total.multiply(0.8).with(Monetary.getDefaultRounding()).add(Money.of(amountDining*15, EURO));
		}
	}

	/**
	 * returns true if the order is discounted or not
	 * @return
	 */
	public Boolean isDiscounted(){
		return !(deliver && !coupon);
	}

	/**
	 * calculates the discount depending on Attributes attached to the order
	 * @return
	 */
	public MonetaryAmount getDiscount(){
		MonetaryAmount total = super.getTotalPrice().subtract(Money.of(amountDining*15,EURO));
		if((!deliver && !coupon) || (deliver && coupon)) {
			return total.multiply(-0.1).with(Monetary.getDefaultRounding());
		} else {
			return total.multiply(-0.2).with(Monetary.getDefaultRounding());
		}
	}


	/**
	 * sets the delivery state
	 * @param b
	 */
	public void setDeliverableState (boolean b){
		deliver = b;
	}

	/**
	 * returns true, if the Order is going to be delivered
	 * @return
	 */
	public boolean getDeliverableState(){
		return deliver;
	}

	/**
	 * sets the completed state to true, can't be reversed
	 */
	public void isNowCompleted(){
		completed = true;
	}

	/**
	 * return true if the otder is completed
	 * @return
	 */
	public boolean getCompletedState(){
		return completed;
	}

	/**
	 * increases the total amount of pizzas stored in our order
	 */
	public void increaseAmount(){
		amountOfPizzen++;
	}

	/**
	 * decreases the amount of pizza objects, if a pizza finished baking
	 */
	public void decreaseAmount(){
		if (amountOfPizzen > 0) {
			amountOfPizzen--;
		}
	}

	/**
	 * stores the amount of pizzas included
	 * @return
	 */
	public int getAmountOfPizzen(){
		return amountOfPizzen;
	}

	/**
	 * returns the estimated deliverytime
	 * @return
	 */
	public long getGuessedDeliveryTime() {
		return guessedDeliveryTime;
	}

	/**
	 * Stores the estimated delivery time
	 * @param guessedDeliveryTime
	 */
	public void setGuessedDeliveryTime(long guessedDeliveryTime) {
		this.guessedDeliveryTime = guessedDeliveryTime;
	}
}
