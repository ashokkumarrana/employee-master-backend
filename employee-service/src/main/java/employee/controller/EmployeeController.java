package employee.controller;
import employee.dto.*;
import employee.service.EmployeeService;
import employee.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("employee")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final FileStorageService fileStorageService;

    @PreAuthorize("hasAnyRole('ADMIN','MANAGEMENT','HOD')")
    @PostMapping("save")
    public ResponseEntity<ApiResponseDto<?>> saveEmployees(@Valid @RequestBody List<@Valid EmployeeRequestDto> requestDto) {
        return employeeService.addOrUpdateEmployees(requestDto);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGEMENT','HOD')")
    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<?>> uploadAttachments(@RequestParam MultiValueMap<String, MultipartFile> files) {
        return employeeService.uploadAttachments(files);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER','MANAGEMENT','HOD')")
    @GetMapping("list")
    public ResponseEntity<ApiResponseDto<?>> getAllEmployees(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "desc") String direction, @ModelAttribute EmployeeFilterRequestDto filter) {
        return employeeService.getAllEmployees(page, size, sortBy, direction, filter);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER','MANAGEMENT','HOD')")
    @GetMapping("{id}")
    public ResponseEntity<ApiResponseDto<?>> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER','MANAGEMENT','HOD')")
    @GetMapping("history/{employeeId}")
    public ResponseEntity<ApiResponseDto<?>> getEmployeeHistory(@PathVariable Long employeeId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "changedAt") String sortBy, @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(employeeService.getEmployeeHistory(employeeId, page, size, sortBy, direction));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("delete/{id}")
    public ResponseEntity<ApiResponseDto<?>> deleteEmployeeById(@PathVariable Long id) {
        return employeeService.deleteEmployeeById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER','MANAGEMENT','HOD')")
    @GetMapping("count-status")
    public ResponseEntity<ApiResponseDto<Map<String, Long>>> getEmployeeCounts(@RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) LocalDate toDate) {
        ApiResponseDto<Map<String, Long>> response = employeeService.getEmployeeCounts(fromDate, toDate);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER','MANAGEMENT','HOD')")
    @GetMapping("/search")
    public ApiResponseDto<List<EmployeeResponseDto>> search(EmployeeSearchRequestDto request) {
        return employeeService.search(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER','MANAGEMENT','HOD')")
    @GetMapping("/department/count-department")
    public ApiResponseDto<List<DepartmentEmployeeCountDto>> getDepartmentEmployeeCounts(@RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) LocalDate toDate, @RequestParam(required = false) Boolean status) {
        return employeeService.getDepartmentEmployeeCounts(fromDate, toDate, status);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER','MANAGEMENT','HOD')")
    @GetMapping("/attachments/download")
    public ResponseEntity<Resource> downloadAttachment(@RequestParam String path) {
        Resource resource = fileStorageService.loadFileAsResource(path);
        String contentType = fileStorageService.detectContentType(path);
        String filename = fileStorageService.getOriginalFileName(path);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"").body(resource);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER','MANAGEMENT','HOD')")
    @GetMapping("/attachments/view")
    public ResponseEntity<Resource> viewAttachment(@RequestParam String path) {
        Resource resource = fileStorageService.loadFileAsResource(path);
        String contentType = fileStorageService.detectContentType(path);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).header(HttpHeaders.CONTENT_DISPOSITION, "inline").body(resource);
    }
}
