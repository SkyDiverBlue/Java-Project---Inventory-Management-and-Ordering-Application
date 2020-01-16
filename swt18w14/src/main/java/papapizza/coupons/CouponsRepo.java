package papapizza.coupons;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


/**
 * The CouponsRepo contains all Coupons
 */
@Repository
public interface CouponsRepo extends CrudRepository<Coupon, Long> {



}
