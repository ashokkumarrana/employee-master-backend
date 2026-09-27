package employee.dto;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class EmployeeRequestDto {
    private Long id;
    @NotBlank(message = "Employee code is required")
    @Size(max = 20, message = "Employee code must not exceed 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Employee code must contain only alphanumeric characters")
    private String employeeCode;
    @NotBlank(message = "Employee name is required")
    @Size(max = 150, message = "Employee name must not exceed 150 characters")
    @Pattern(regexp = "^[A-Za-z]+(?: [A-Za-z]+)*$", message = "Employee name must contain alphabets and spaces only")
    private String employeeName;
    @Size(max = 150, message = "Communication name must not exceed 150 characters")
    private String communicationName;
    @NotNull(message = "Department is required")
    private Long departmentId;
    @NotNull(message = "Designation is required")
    private Long designationId;
    private Long reportingManager;
    @NotBlank(message = "Employee type is required")
    @Pattern(regexp = "^(Permanent|Contract|Consultant)$", message = "Employee type must be Permanent, Contract or Consultant")
    private String employeeType;
    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female or Other")
    private String gender;
    @Pattern(regexp = "^(Married|Unmarried)?$", message = "Marital status must be Married or Unmarried")
    private String maritalStatus;
    private List<String> skills;
    @Size(max = 100, message = "Languages must not exceed 100 characters")
    private String languages;
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Mobile number must be a valid 10 digit number starting with 6, 7, 8 or 9")
    private String mobile;
    @Pattern(regexp = "^$|^[6-9][0-9]{9}$", message = "Alternate mobile number must be a valid 10 digit number starting with 6, 7, 8 or 9")
    private String alternateMobile;
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;
    @Email(message = "Please enter a valid alternate email address")
    @Size(max = 150, message = "Alternate email must not exceed 150 characters")
    private String alternateEmail;
    @PastOrPresent
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;
    @NotNull(message = "Joining date is required")
    @PastOrPresent(message = "Joining date cannot be a future date")
    private LocalDate joiningDate;
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Pattern(regexp = "^$|^[A-Za-z]+(?: [A-Za-z]+)*$", message = "City must contain alphabets and spaces only")
    private String city;
    private Long stateId;
    @NotNull(message = "Country is required")
    private Long countryId;
    @Size(max = 10, message = "Zip code must not exceed 10 characters")
    @Size(max = 10, message = "Zip code must not exceed 10 characters")
    @Pattern(regexp = "^$|^[0-9]+$", message = "Zip code must contain numbers only")
    private String zipCode;
    @Pattern(regexp = "^(A\\+|A-|B\\+|B-|AB\\+|AB-|O\\+|O-)?$", message = "Please enter a valid blood group")
    private String bloodGroup;
    private boolean status;
    private MultipartFile profileImage;
    private MultipartFile documents;
    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    private String remarks;

    @JsonSetter("employeeCode")
    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode == null ? null : employeeCode.trim();
    }

    @JsonSetter("employeeName")
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName == null ? null : employeeName.trim();
    }

    @JsonSetter("communicationName")
    public void setCommunicationName(String communicationName) {
        this.communicationName = communicationName == null ? null : communicationName.trim();
    }

    @JsonSetter("employeeType")
    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType == null ? null : employeeType.trim();
    }

    @JsonSetter("gender")
    public void setGender(String gender) {
        this.gender = gender == null ? null : gender.trim();
    }

    @JsonSetter("maritalStatus")
    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus == null ? null : maritalStatus.trim();
    }

    @JsonSetter("email")
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    @JsonSetter("mobile")
    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    @JsonSetter("alternateMobile")
    public void setAlternateMobile(String alternateMobile) {
        this.alternateMobile = alternateMobile == null ? null : alternateMobile.trim();
    }

    @JsonSetter("city")
    public void setCity(String city) {
        this.city = city == null ? null : city.trim();
    }

    @JsonSetter("address")
    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    @JsonSetter("zipCode")
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode == null ? null : zipCode.trim();
    }

    @JsonSetter("bloodGroup")
    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup == null ? null : bloodGroup.trim();
    }

    @JsonSetter("remarks")
    public void setRemarks(String remarks) {
        this.remarks = remarks == null ? null : remarks.trim();
    }
}