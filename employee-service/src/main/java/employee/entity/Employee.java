package employee.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;
    @Column(length = 20, nullable = false, unique = true)
    private String employeeCode;
    @Column(length = 150, nullable = false)
    private String employeeName;
    @Column(length = 150)
    private String communicationName;
    @Column(nullable = false)
    private Long departmentId;
    @Column(nullable = false)
    private Long designationId;
    private Long reportingManager;
    @Column(length = 20, nullable = false)
    private String employeeType;
    @Column(length = 20, nullable = false)
    private String gender;
    @Column(length = 20)
    private String maritalStatus;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> skills;
    @Column(length = 100)
    private String languages;
    @Column(length = 15, nullable = false, unique = true)
    private String mobile;
    @Column(length = 15)
    private String alternateMobile;
    @Column(length = 150, nullable = false, unique = true)
    private String email;
    @Column(length = 150)
    private String alternateEmail;
    private LocalDate dob;
    @Column(nullable = false)
    private LocalDate joiningDate;
    @Column(columnDefinition = "TEXT")
    private String address;
    @Column(length = 100)
    private String city;
    private Long stateId;
    @Column(nullable = false)
    private Long countryId;
    @Column(length = 10)
    private String zipCode;
    @Column(length = 10)
    private String bloodGroup;
    @Column(nullable = false)
    private boolean status;
    private String profileImage;
    private String documents;
    @Column(columnDefinition = "TEXT")
    private String remarks;
    @CreatedBy
    private Long createdBy;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
}