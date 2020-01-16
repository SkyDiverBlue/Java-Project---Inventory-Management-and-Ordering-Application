package papapizza.bill;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import papapizza.order.CustomerOrder;

@Repository
public interface OrderRepo extends CrudRepository<CustomerOrder, Long> {

	//CustomerOrder findBy

}
