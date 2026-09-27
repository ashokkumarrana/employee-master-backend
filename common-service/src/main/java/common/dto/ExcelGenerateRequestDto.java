package common.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ExcelGenerateRequestDto {

    @NotBlank(message = "Sheet name is required")
    private String sheetName;
    @NotEmpty(message = "Headers are required")
    private List<String> headers;
    private List<String> fieldTypes;
    private List<Map<String, String>> rows;
    private boolean includeTypeRow;
}
