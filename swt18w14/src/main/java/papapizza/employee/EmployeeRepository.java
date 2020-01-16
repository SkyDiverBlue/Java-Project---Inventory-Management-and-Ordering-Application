package papapizza.employee;

import org.springframework.data.repository.CrudRepository;

/**
 * The Repository which stores every employee.
 */
public interface EmployeeRepository extends CrudRepository<Employee, Long> { }
