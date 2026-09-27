package employee.dto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeFilterRequestDto {

    private Long departmentId;
    private Long designationId;
    private String city;
    private Boolean status;
    private LocalDate fromDate;
    private LocalDate toDate;
}