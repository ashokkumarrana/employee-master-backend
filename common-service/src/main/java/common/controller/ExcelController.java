package common.controller;
import common.dto.ExcelGenerateRequestDto;
import common.dto.ExcelParseRequestDto;
import common.dto.ExcelParseResponseDto;
import common.util.ExcelUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/common/excel")
public class ExcelController {

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(@Valid @RequestBody ExcelGenerateRequestDto request) {
        byte[] file = ExcelUtil.generateExcel(request.getSheetName(), request.getHeaders(), request.getFieldTypes(), request.getRows(), request.isIncludeTypeRow());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + request.getSheetName() + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(file.length)
                .body(file);
    }

    @PostMapping("/parse")
    public ResponseEntity<ExcelParseResponseDto> parse(@Valid @RequestBody ExcelParseRequestDto request) {
        List<LinkedHashMap<String, String>> parsedRows = ExcelUtil.parseAndValidateExcelRows(request.getFileContent(), request.getHeaders(), request.getHeaderRowIndex());
        List<Map<String, String>> rows = new java.util.ArrayList<>(parsedRows);
        ExcelParseResponseDto response = new ExcelParseResponseDto(rows.size(), request.getHeaders(), rows);
        return ResponseEntity.ok(response);
    }
}
