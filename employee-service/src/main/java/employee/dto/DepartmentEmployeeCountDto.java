package employee.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DepartmentEmployeeCountDto {

    private Long id;
    private String name;
    private Long total;
    private Long active;
    private Long inactive;
}