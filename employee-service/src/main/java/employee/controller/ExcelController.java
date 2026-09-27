package employee.controller;
import employee.dto.ExcelSaveRequestDto;
import employee.dto.ExcelUploadResponseDto;
import employee.service.ExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/employee/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;

    @PreAuthorize("hasAnyRole('ADMIN','MANAGEMENT','HOD')")
    @PostMapping(value = "/excel-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExcelUploadResponseDto> uploadExcelEmployees(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(excelService.uploadEmployees(file));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGEMENT','HOD')")
    @PostMapping("/excel-upload/save")
    public ResponseEntity<ExcelUploadResponseDto> saveEmployees(@RequestBody ExcelSaveRequestDto request) {
        return ResponseEntity.ok(excelService.saveEmployees(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGEMENT','HOD')")
    @GetMapping("/template")
    public ResponseEntity<ByteArrayResource> downloadEmployeeTemplate() {
        byte[] file = excelService.createEmployeeTemplate();
        return fileResponse(file);
    }

@PreAuthorize("hasAnyRole('ADMIN','MANAGEMENT','HOD')")
@GetMapping("/download")
public ResponseEntity<?> downloadEmployees(
        @RequestParam LocalDate fromDate,
        @RequestParam LocalDate toDate,
        @RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) Long designationId,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) Boolean status) {
    return excelService.downloadEmployeeExcel(departmentId, designationId, city, status, fromDate, toDate);
}

    private ResponseEntity<ByteArrayResource> fileResponse(byte[] file) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "employee_template.xlsx").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).contentLength(file.length).body(new ByteArrayResource(file));
    }
}