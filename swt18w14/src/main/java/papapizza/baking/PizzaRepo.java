package papapizza.baking;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import papapizza.order.KindOfPizza;

@Repository
public interface PizzaRepo extends CrudRepository<KindOfPizza, Long> {
}
