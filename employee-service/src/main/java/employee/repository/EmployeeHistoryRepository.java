package employee.repository;
import employee.entity.EmployeeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeHistoryRepository extends JpaRepository<EmployeeHistory, Long> {

    Page<EmployeeHistory> findByEmployeeId(Long employeeId, Pageable pageable);
}