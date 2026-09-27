package employee.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExcelUploadResponseDto {

    private int totalRows;
    private int successCount;
    private int failureCount;
    private int duplicateCount;
    private List<EmployeeExcelDto> correctData;
    private List<EmployeeExcelDto> incorrectData;
    private List<EmployeeExcelDto> duplicateData;
    private String attachmentName;
    private String attachmentContent;
}
