package common.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExcelParseResponseDto {
    private int totalRows;
    private List<String> headers;
    private List<Map<String, String>> rows;
}
