package common.dto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExcelParseRequestDto {

    @NotNull(message = "File content is required")
    private byte[] fileContent;
    @NotEmpty(message = "Expected headers are required")
    private List<String> headers;
    private int headerRowIndex;
}
