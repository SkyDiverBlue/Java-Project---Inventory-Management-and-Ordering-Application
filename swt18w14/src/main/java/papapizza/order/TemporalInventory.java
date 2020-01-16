package papapizza.order;

import org.salespointframework.quantity.Quantity;
import papapizza.catalog.Item;


import java.lang.reflect.Parameter;
import java.util.TreeMap;

class TemporalInventory {

	private TreeMap<Item, Quantity> storeEveryThing = new TreeMap<>();



	TemporalInventory(){	}


	/**
	 * this Method stores all the Used Items and their Quantity and
	 * @return TreeMap<Item, Quantity>
	 */
	public TreeMap<Item,Quantity> getStoreEveryThing(){
		return storeEveryThing;
	}

	/**
	 * Adds some Item and the Quantity to the Map, creates a new Entry if the Items does not exist.
	 * @param item
	 * @param quantity
	 */
	public void addItemToMap(Item item, Quantity quantity){
		if (storeEveryThing.containsKey(item)){
			storeEveryThing.replace(item, storeEveryThing.get(item).add(quantity));
		}
		else {
			storeEveryThing.put(item, quantity);
		}
	}

	/**
	 * Decreases the given Quantity of a given Item and, if the Quantity is zero, deletes the entry
	 * @param item
	 * @param quantity
	 */
	public void removeItem(Item item, Quantity quantity){
		if (storeEveryThing.containsKey(item)){
			Quantity temp = storeEveryThing.get(item).subtract(quantity);
			if (temp.equals(Quantity.of(0))){
				storeEveryThing.remove(item);
			}
			else{
				storeEveryThing.replace(item,temp);
			}
		}
	}


	/**
	 * The method return the Quantity of a given Item, if the Item does not exists, it simply returns 0;
	 * @param item
	 * @return Quantity
	 */
	public Quantity getQuantityForItem(Item item){
		if (storeEveryThing.containsKey(item)){
			return storeEveryThing.get(item);
		}
		else return Quantity.of(0);
	}


	/**
	 *clears the Map
	 */
	public void clearAll(){
		storeEveryThing = new TreeMap<Item, Quantity>();
		//customer = null;
		System.out.println("ich werde gelöscht");
	}

}
