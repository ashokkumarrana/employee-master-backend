package employee.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class EmployeeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long historyId;
    @Column(nullable = false)
    private Long employeeId;
    @Column(length = 20, nullable = false)
    private String action;
    @Column(length = 20)
    private String employeeCode;
    @Column(length = 150)
    private String employeeName;
    @Column(length = 150)
    private String communicationName;
    private Long departmentId;
    private Long designationId;
    private Long reportingManager;
    @Column(length = 20)
    private String employeeType;
    @Column(length = 20)
    private String gender;
    @Column(length = 20)
    private String maritalStatus;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> skills;
    @Column(length = 100)
    private String languages;
    @Column(length = 15)
    private String mobile;
    @Column(length = 15)
    private String alternateMobile;
    @Column(length = 150)
    private String email;
    @Column(length = 150)
    private String alternateEmail;
    private LocalDate dob;
    private LocalDate joiningDate;
    @Column(columnDefinition = "TEXT")
    private String address;
    @Column(length = 100)
    private String city;
    private Long stateId;
    private Long countryId;
    @Column(length = 10)
    private String zipCode;
    @Column(length = 10)
    private String bloodGroup;
    private Boolean status;
    private String profileImage;
    private String documents;
    @Column(columnDefinition = "TEXT")
    private String remarks;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private Long changedBy;
    private LocalDateTime changedAt;
}
