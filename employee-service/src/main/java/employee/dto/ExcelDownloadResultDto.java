package employee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExcelDownloadResultDto {
    private boolean emailSent;
    private String message;
    private String fileName;
    private byte[] fileContent;
}
