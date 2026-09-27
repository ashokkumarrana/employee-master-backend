package employee.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeResponseDto {
    private Long id;
    private String employeeCode;
    private String employeeName;
    private String communicationName;
    private Long departmentId;
    private Long designationId;
    private Long reportingManager;
    private String employeeType;
    private String gender;
    private String maritalStatus;
    private List<String> skills;
    private String languages;
    private String mobile;
    private String alternateMobile;
    private String email;
    private String alternateEmail;
    private LocalDate dob;
    private LocalDate joiningDate;
    private String address;
    private String city;
    private Long stateId;
    private Long countryId;
    private String zipCode;
    private String bloodGroup;
    private Boolean status;
    private String profileImage;
    private String documents;
    private String remarks;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
}
