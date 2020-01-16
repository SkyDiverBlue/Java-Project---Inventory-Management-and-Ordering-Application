package papapizza.customer;

import org.springframework.stereotype.Repository;
import papapizza.customer.Customer;

import org.springframework.data.repository.CrudRepository;

@Repository
interface CustomerRepository extends CrudRepository<Customer, Long> {}
