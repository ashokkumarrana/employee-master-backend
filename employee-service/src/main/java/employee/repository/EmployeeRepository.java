package employee.repository;
import employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("select count(e) > 0 from Employee e where e.status = false and LOWER(e.employeeCode) = LOWER(:employeeCode) and (:id is null or e.id <> :id)")
    boolean existsDuplicateEmployeeCode(@Param("employeeCode") String employeeCode, @Param("id") Long id);

    @Query("select count(e) > 0 from Employee e where e.status = false and lower(e.email) = lower(:email) and (:id is null or e.id <> :id)")
    boolean existsDuplicateEmployeeEmail(@Param("email") String email, @Param("id") Long id);

    @Query("select count(e) > 0 from Employee e where e.status = false and e.mobile = :mobile and (:id is null or e.id <> :id)")
    boolean existsDuplicateEmployeeMobile(@Param("mobile") String mobile, @Param("id") Long id);

    long countByStatusFalse();

    long countByStatusTrue();

    @Query("""

            select
        count(e),
        sum(case when e.status = false then 1 else 0 end),
        sum(case when e.status = true then 1 else 0 end)
    from Employee e
    where (:fromDate is null or cast(e.createdAt as date) >= :fromDate)
      and (:toDate is null or cast(e.createdAt as date) <= :toDate)
    """)
    List<Object[]> getEmployeeCountsByDateRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("select new employee.dto.EmployeeExcelValidationDto(e.id, e.employeeCode, e.employeeName, e.email, e.mobile) from Employee e where e.status=false or e.status=true")
    List<employee.dto.EmployeeExcelValidationDto> findAllForExcelValidation();

    @Query("""
    select e from Employee e
      where (
          cast(e.id as string) = :search
          or lower(e.employeeCode) = lower(:search)
          or lower(e.employeeName) = lower(:search)
          or e.mobile = :search
          or lower(e.email) = lower(:search)
          or lower(e.employeeType) = lower(:search)
          or e.alternateMobile = :search
      )
    order by e.id desc
    """)
    Page<Employee> searchEmployees(@Param("search") String search, Pageable pageable);

    @Query("""
        select e.departmentId,
               count(e.id),
               sum(case when e.status = false then 1 else 0 end),
               sum(case when e.status = true then 1 else 0 end)
        from Employee e
        where (:fromDate is null or e.joiningDate >= :fromDate)
          and (:toDate is null or e.joiningDate <= :toDate)
          and (:status is null or e.status = :status)
        group by e.departmentId
        order by e.departmentId
        """)
    List<Object[]> getDepartmentEmployeeCounts(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") Boolean status
    );

@Query("""
            select e from Employee e
            where (:departmentId is null or e.departmentId = :departmentId)
              and (:designationId is null or e.designationId = :designationId)
              and (:city is null or :city = '' or lower(e.city) = lower(:city))
              and (:status is null or e.status = :status)
              and (:fromDate is null or cast(e.createdAt as date) >= :fromDate)
              and (:toDate is null or cast(e.createdAt as date) <= :toDate)
            """)
Page<Employee> findEmployees(@Param("departmentId") Long departmentId, @Param("designationId") Long designationId, @Param("city") String city, @Param("status") Boolean status, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);

@Query("""
    select e from Employee e
    where (:departmentId is null or e.departmentId = :departmentId)
      and (:designationId is null or e.designationId = :designationId)
      and (:city is null or :city = '' or lower(e.city) = lower(:city))
      and (:status is null or e.status = :status)
      and (:fromDate is null or cast(e.createdAt as date) >= :fromDate)
      and (:toDate is null or cast(e.createdAt as date) <= :toDate)
    order by e.id desc
    """)
List<Employee> findAllForExcelDownload(@Param("departmentId") Long departmentId,
                                       @Param("designationId") Long designationId,
                                       @Param("city") String city,
                                       @Param("status") Boolean status,
                                       @Param("fromDate") LocalDate fromDate,
                                       @Param("toDate") LocalDate toDate);
}