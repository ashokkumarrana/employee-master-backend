package employee.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeExcelValidationDto {

    private Long id;
    private String employeeCode;
    private String employeeName;
    private String email;
    private String mobile;
}
