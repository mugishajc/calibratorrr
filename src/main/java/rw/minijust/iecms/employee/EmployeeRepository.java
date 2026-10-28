package rw.minijust.iecms.employee;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring generates the SQL from the method name
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartment(String department);

    List<Employee> findBySalaryGreaterThan(Double amount);
}
