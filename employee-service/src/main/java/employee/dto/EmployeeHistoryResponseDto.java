package employee.dto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class EmployeeHistoryResponseDto {
    private Long historyId;
    private Long employeeId;
    private String action;
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
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private Long changedBy;
    private String changedByName;
    private LocalDateTime changedAt;
}