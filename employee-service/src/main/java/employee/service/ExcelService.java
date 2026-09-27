package employee.service;
import common.dto.ExcelGenerateRequestDto;
import common.util.EmployeeExcelHeaders;
import common.service.EmailService;
import common.util.ExcelUtil;
import employee.client.AuthServiceClient;
import employee.client.DropdownClient;
import employee.dto.*;
import employee.entity.Employee;
import employee.exception.EmployeeException;
import employee.exception.InvalidExcelFileException;
import employee.repository.EmployeeRepository;
import employee.util.ExcelHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;
    private final DropdownClient dropdownClient;
    private final AuthServiceClient authServiceClient;
    private final EmailService emailService;
    @Value("${app.notification.email}")
    private String notificationEmail;
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    private static final int MAX_DIRECT_DOWNLOAD_DAYS = 7;
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_NAME_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public ExcelUploadResponseDto uploadEmployees(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidExcelFileException("Excel file cannot be empty");
        }
        byte[] fileContent;
        try {
            fileContent = file.getBytes();
        } catch (IOException ex) {
            throw new InvalidExcelFileException("Failed to read Excel file");
        }
        List<LinkedHashMap<String, String>> parsedRows = ExcelUtil.parseAndValidateExcelRows(fileContent, EmployeeExcelHeaders.HEADERS, 1);
        if (parsedRows.isEmpty()) {
            throw new InvalidExcelFileException("No employee data found in the Excel file");
        }
        List<EmployeeExcelDto> excelList = new ArrayList<>();
        int fallbackRowNumber = 3;
        for (Map<String, String> row : parsedRows) {
            excelList.add(ExcelHelper.fromRow(row, fallbackRowNumber++));
        }
        Map<Long, String> departmentMap = dropdownClient.getDepartmentMap();
        Map<Long, String> designationMap = dropdownClient.getDesignationMap();
        Map<Long, String> stateMap = dropdownClient.getStateMap();
        Map<Long, String> countryMap = dropdownClient.getCountryMap();
        List<EmployeeExcelValidationDto> existingEmployees = employeeRepository.findAllForExcelValidation();
        Set<String> existingEmployeeCodes = existingEmployees.stream().map(EmployeeExcelValidationDto::getEmployeeCode).filter(Objects::nonNull).map(code -> code.trim().toLowerCase()).collect(Collectors.toSet());
        Set<String> existingEmails = existingEmployees.stream().map(EmployeeExcelValidationDto::getEmail).filter(Objects::nonNull).map(email -> email.trim().toLowerCase()).collect(Collectors.toSet());
        Set<String> existingMobiles = existingEmployees.stream().map(EmployeeExcelValidationDto::getMobile).filter(Objects::nonNull).map(String::trim).collect(Collectors.toSet());
        Set<String> excelDuplicateEmployeeCodes = findExcelDuplicates(excelList, EmployeeExcelDto::getEmployeeCode);
        Set<String> excelDuplicateEmails = findExcelDuplicates(excelList, EmployeeExcelDto::getEmail);
        Set<String> excelDuplicateMobiles = findExcelDuplicates(excelList, EmployeeExcelDto::getMobile);
        List<EmployeeExcelDto> correctData = new ArrayList<>();
        List<EmployeeExcelDto> incorrectData = new ArrayList<>();
        List<EmployeeExcelDto> duplicateData = new ArrayList<>();
        for (EmployeeExcelDto excel : excelList) {
            List<String> validationErrors = new ArrayList<>();
            validateRow(excel, departmentMap, designationMap, stateMap, countryMap, validationErrors);
            List<String> duplicateErrors = new ArrayList<>();
            checkDuplicate(safe(excel.getEmployeeCode()), excelDuplicateEmployeeCodes, existingEmployeeCodes, "Employee Code", duplicateErrors);
            checkDuplicate(safe(excel.getEmail()), excelDuplicateEmails, existingEmails, "Email", duplicateErrors);
            checkDuplicate(safe(excel.getMobile()), excelDuplicateMobiles, existingMobiles, "Mobile Number", duplicateErrors);
            if (!validationErrors.isEmpty()) {
                excel.setErrorMessage(String.join("; ", validationErrors));
                incorrectData.add(excel);
            } else if (!duplicateErrors.isEmpty()) {
                excel.setErrorMessage(String.join("; ", duplicateErrors));
                duplicateData.add(excel);
            } else {
                excel.setErrorMessage(null);
                correctData.add(excel);
            }
        }
        return ExcelUploadResponseDto.builder().totalRows(excelList.size()).successCount(correctData.size()).failureCount(incorrectData.size()).duplicateCount(duplicateData.size()).correctData(correctData).incorrectData(incorrectData).duplicateData(duplicateData).build();
    }

    private void validateRow(EmployeeExcelDto excel, Map<Long, String> departmentMap, Map<Long, String> designationMap, Map<Long, String> stateMap, Map<Long, String> countryMap, List<String> errors) {
        requireField(excel.getEmployeeCode(), "Employee Code", errors);
        requireField(excel.getEmployeeName(), "Employee Name", errors);
        requireField(excel.getEmployeeType(), "Employee Type", errors);
        requireField(excel.getGender(), "Gender", errors);
        requireField(excel.getMobile(), "Mobile", errors);
        requireField(excel.getEmail(), "Email", errors);
        validateMobile(excel.getMobile(), "Mobile", errors);
        validateMobile(excel.getAlternateMobile(), "Alternate Mobile", errors);
        validateReference(excel.getDepartmentName(), departmentMap, "Department", true, errors);
        validateReference(excel.getDesignationName(), designationMap, "Designation", true, errors);
        validateReference(excel.getStateName(), stateMap, "State", false, errors);
        validateReference(excel.getCountryName(), countryMap, "Country", true, errors);
        validateDob(excel.getDob(), errors);
        validateJoiningDate(excel.getJoiningDate(), errors);
    }

    private void validateMobile(String value, String label, List<String> errors) {
        String mobile = safe(value);
        if (mobile.isEmpty()) {
            return;
        }
        if (!MOBILE_PATTERN.matcher(mobile).matches()) {
            errors.add(label + " must be a valid 10-digit mobile number.");
        }
    }

    private void requireField(String value, String label, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(label + " is required.");
        }
    }

    private void validateReference(String name, Map<Long, String> referenceMap, String label, boolean required, List<String> errors) {
        String value = safe(name);
        if (value.isEmpty()) {
            if (required) {
                errors.add(label + " is required.");
            }
            return;
        }
        boolean exists = referenceMap.values().stream().anyMatch(existing -> existing.equalsIgnoreCase(value));
        if (!exists) {
            errors.add(label + " not found: " + value);
        }
    }

    private void validateDob(String dob, List<String> errors) {
        String value = safe(dob);
        if (value.isEmpty()) {
            return;
        }
        try {
            LocalDate date = LocalDate.parse(value);
            if (Period.between(date, LocalDate.now()).getYears() < 18) {
                errors.add("Employee age must be at least 18 years.");
            }
        } catch (DateTimeParseException ex) {
            errors.add("DOB must be in yyyy-MM-dd format.");
        }
    }

    private void validateJoiningDate(String joiningDate, List<String> errors) {
        String value = safe(joiningDate);
        if (value.isEmpty()) {
            errors.add("Joining Date is required.");
            return;
        }
        try {
            LocalDate date = LocalDate.parse(value);
            if (date.isAfter(LocalDate.now())) {
                errors.add("Joining date cannot be a future date.");
            }
        } catch (DateTimeParseException ex) {
            errors.add("Joining Date must be in yyyy-MM-dd format.");
        }
    }

    private void checkDuplicate(String value, Set<String> excelDuplicates, Set<String> existingSeen, String label, List<String> errors) {
        if (value.isEmpty()) {
            return;
        }
        String normalized = value.toLowerCase();
        if (excelDuplicates.contains(normalized)) {
            errors.add("Duplicate " + label + " found in Excel.");
        } else if (existingSeen.contains(normalized)) {
            errors.add(label + " already exists in the database.");
        }
    }

    private Set<String> findExcelDuplicates(List<EmployeeExcelDto> excelList, java.util.function.Function<EmployeeExcelDto, String> fieldExtractor) {
        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new HashSet<>();
        for (EmployeeExcelDto excel : excelList) {
            String value = safe(fieldExtractor.apply(excel));
            if (value.isEmpty()) {
                continue;
            }
            String normalized = value.toLowerCase();
            if (!seen.add(normalized)) {
                duplicates.add(normalized);
            }
        }
        return duplicates;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public ExcelUploadResponseDto saveEmployees(ExcelSaveRequestDto request) {
        if (request == null || request.getCorrectData() == null || request.getCorrectData().isEmpty()) {
            throw new InvalidExcelFileException("No valid employee data available to save");
        }
        Map<Long, String> departmentMap = dropdownClient.getDepartmentMap();
        Map<Long, String> designationMap = dropdownClient.getDesignationMap();
        Map<Long, String> stateMap = dropdownClient.getStateMap();
        Map<Long, String> countryMap = dropdownClient.getCountryMap();
        List<EmployeeRequestDto> employees = request.getCorrectData().stream().map(dto -> convertToEmployeeRequest(dto, departmentMap, designationMap, stateMap, countryMap)).toList();
        var response = employeeService.addOrUpdateEmployees(employees);
        if (response == null || response.getBody() == null || response.getBody().getData() == null) {
            throw new InvalidExcelFileException("Employees could not be saved");
        }
        String resultFileName = "employee-import-result-" + LocalDateTime.now().format(FILE_NAME_TIME_FORMAT) + ".xlsx";
        byte[] resultFile = buildImportResultFile(request);
        notifyExcelUploadCompleted(request, resultFileName, resultFile);
        return ExcelUploadResponseDto.builder().totalRows(request.getTotalRows()).successCount(request.getSuccessCount()).failureCount(request.getFailureCount()).duplicateCount(request.getDuplicateCount()).correctData(request.getCorrectData()).incorrectData(List.of()).duplicateData(List.of()).attachmentName(resultFile != null ? resultFileName : null).attachmentContent(resultFile != null ? Base64.getEncoder().encodeToString(resultFile) : null).build();
    }

    private byte[] buildImportResultFile(ExcelSaveRequestDto request) {
        try {
            List<Map<String, String>> resultRows = ExcelHelper.toImportResultRows(request.getCorrectData(), request.getIncorrectData(), request.getDuplicateData());
            return ExcelUtil.generateWithStatusColors("Import Result", EmployeeExcelHeaders.IMPORT_RESULT_HEADERS, resultRows, EmployeeExcelHeaders.IMPORT_STATUS_HEADER);
        } catch (Exception ex) {
            log.error("Failed to generate Excel import result file", ex);
            return null;
        }
    }

    private void notifyExcelUploadCompleted(ExcelSaveRequestDto request, String resultFileName, byte[] resultFile) {
        try {
            int savedCount = sizeOf(request.getCorrectData());
            int invalidCount = sizeOf(request.getIncorrectData());
            int duplicateCount = sizeOf(request.getDuplicateData());
            emailService.sendImportSummaryAsync(notificationEmail, savedCount, invalidCount, duplicateCount, resultFileName, resultFile);
        } catch (Exception ex) {
            log.error("Failed to queue Excel import notification email", ex);
        }
    }

    private int sizeOf(List<EmployeeExcelDto> list) {
        return list == null ? 0 : list.size();
    }

    public byte[] createEmployeeTemplate() {
        ExcelGenerateRequestDto request = new ExcelGenerateRequestDto();
        request.setSheetName("Employees");
        request.setHeaders(EmployeeExcelHeaders.HEADERS);
        request.setFieldTypes(EmployeeExcelHeaders.FIELD_TYPES);
        request.setIncludeTypeRow(true);
        request.setRows(ExcelHelper.toRows(List.of(ExcelHelper.createDummyEmployeeData())));
        return ExcelUtil.generateExcel(request.getSheetName(), request.getHeaders(), request.getFieldTypes(), request.getRows(), request.isIncludeTypeRow());
    }

    public ResponseEntity<?> downloadEmployeeExcel(Long departmentId, Long designationId, String city, Boolean status, LocalDate fromDate, LocalDate toDate) {
        ExcelDownloadResultDto result = prepareEmployeeExcelDownload(departmentId, designationId, city, status, fromDate, toDate);
        if (result.isEmailSent()) {
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("fileName", result.getFileName());
            if (result.getFileContent() != null) {
                emailData.put("fileContent", Base64.getEncoder().encodeToString(result.getFileContent()));
            }
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponseDto<>(HttpStatus.ACCEPTED.value(), result.getMessage(), emailData, "none"));
        }
        return ExcelUtil.buildExcelDownloadResponse(result.getFileContent(), result.getFileName());
    }

    public ExcelDownloadResultDto prepareEmployeeExcelDownload(Long departmentId, Long designationId, String city, Boolean status, LocalDate fromDate, LocalDate toDate) {
        if (Objects.isNull(fromDate) || Objects.isNull(toDate))
            throw new EmployeeException(400, "From date and To date are required.", HttpStatus.BAD_REQUEST);
        if (fromDate.isAfter(toDate))
            throw new EmployeeException(400, "From date cannot be greater than To date.", HttpStatus.BAD_REQUEST);
        long selectedDays = ChronoUnit.DAYS.between(fromDate, toDate);
        String fileName = "employees_" + fromDate + "_to_" + toDate + ".xlsx";
        byte[] file = createEmployeeExcel(departmentId, designationId, city, status, fromDate, toDate);
        if (selectedDays <= MAX_DIRECT_DOWNLOAD_DAYS)
            return new ExcelDownloadResultDto(false, "File is ready to download.", fileName, file);
        emailService.sendExcelReportAsync(notificationEmail, "Employee Excel Report (" + fromDate + " to " + toDate + ")", fileName, file);
        return new ExcelDownloadResultDto(true, "Date range exceeds 7 days. Report sent by email.", fileName, file);
    }

    public byte[] createEmployeeExcel(Long departmentId, Long designationId, String city, Boolean status, LocalDate fromDate, LocalDate toDate) {
        List<Employee> employees = employeeRepository.findAllForExcelDownload(departmentId, designationId, city, status, fromDate, toDate);
        if (employees.isEmpty()) {
            throw new EmployeeException(404, "No employees found for the selected date range.", HttpStatus.NOT_FOUND);
        }
        Map<Long, String> departmentMap = dropdownClient.getDepartmentMap();
        Map<Long, String> designationMap = dropdownClient.getDesignationMap();
        Map<Long, String> stateMap = dropdownClient.getStateMap();
        Map<Long, String> countryMap = dropdownClient.getCountryMap();
        Map<Long, String> userNames = getUserNameMap(employees);
        List<EmployeeExcelDto> data = employees.stream().map(e -> convertToExcelDto(e, departmentMap, designationMap, stateMap, countryMap, userNames)).toList();
        ExcelGenerateRequestDto request = new ExcelGenerateRequestDto();
        request.setSheetName("Employees");
        request.setHeaders(EmployeeExcelHeaders.DOWNLOAD_HEADERS);
        request.setFieldTypes(EmployeeExcelHeaders.DOWNLOAD_FIELD_TYPES);
        request.setIncludeTypeRow(true);
        request.setRows(ExcelHelper.toDownloadRows(data));
        return ExcelUtil.generateExcel(request.getSheetName(), request.getHeaders(), request.getFieldTypes(), request.getRows(), request.isIncludeTypeRow());
    }

    private Map<Long, String> getUserNameMap(List<Employee> employees) {
        Set<Long> userIds = new HashSet<>();
        employees.forEach(e -> {
            if (e.getCreatedBy() != null) userIds.add(e.getCreatedBy());
            if (e.getUpdatedBy() != null) userIds.add(e.getUpdatedBy());
        });
        Map<Long, String> names = new HashMap<>();
        for (Long id : userIds) {
            try {
                UserResponseDto user = authServiceClient.getUserById(id);
                names.put(id, user != null ? user.getName() : null);
            } catch (Exception ex) {
                log.warn("Unable to fetch user: {}", id, ex);
            }
        }
        return names;
    }

    private EmployeeExcelDto convertToExcelDto(Employee employee, Map<Long, String> departmentMap, Map<Long, String> designationMap, Map<Long, String> stateMap, Map<Long, String> countryMap, Map<Long, String> userNames) {
        EmployeeExcelDto dto = new EmployeeExcelDto();
        dto.setEmployeeCode(employee.getEmployeeCode());
        dto.setEmployeeName(employee.getEmployeeName());
        dto.setCommunicationName(employee.getCommunicationName());
        dto.setDepartmentName(departmentMap.get(employee.getDepartmentId()));
        dto.setDesignationName(designationMap.get(employee.getDesignationId()));
        dto.setReportingManagerCode(employee.getReportingManager() != null ? employee.getReportingManager().toString() : null);
        dto.setEmployeeType(employee.getEmployeeType());
        dto.setGender(employee.getGender());
        dto.setMaritalStatus(employee.getMaritalStatus());
        dto.setSkills(employee.getSkills() != null ? String.join(", ", employee.getSkills()) : null);
        dto.setLanguages(employee.getLanguages());
        dto.setMobile(employee.getMobile());
        dto.setAlternateMobile(employee.getAlternateMobile());
        dto.setEmail(employee.getEmail());
        dto.setAlternateEmail(employee.getAlternateEmail());
        if (employee.getDob() != null) {
            dto.setDob(employee.getDob().toString());
        }
        if (employee.getJoiningDate() != null) {
            dto.setJoiningDate(employee.getJoiningDate().toString());
        }
        dto.setAddress(employee.getAddress());
        dto.setCity(employee.getCity());
        dto.setStateName(stateMap.get(employee.getStateId()));
        dto.setCountryName(countryMap.get(employee.getCountryId()));
        dto.setZipCode(employee.getZipCode());
        dto.setBloodGroup(employee.getBloodGroup());
        dto.setStatus(employee.isStatus() ? "Inactive" : "Active");
        dto.setRemarks(employee.getRemarks());
        dto.setCreatedBy(employee.getCreatedBy() != null ? userNames.get(employee.getCreatedBy()) : null);
        dto.setCreatedAt(employee.getCreatedAt() != null ? employee.getCreatedAt().format(DATE_TIME_FORMAT) : null);
        dto.setUpdatedBy(employee.getUpdatedBy() != null ? userNames.get(employee.getUpdatedBy()) : null);
        dto.setUpdatedAt(employee.getUpdatedAt() != null ? employee.getUpdatedAt().format(DATE_TIME_FORMAT) : null);
        return dto;
    }

    private EmployeeRequestDto convertToEmployeeRequest(EmployeeExcelDto dto, Map<Long, String> departmentMap, Map<Long, String> designationMap, Map<Long, String> stateMap, Map<Long, String> countryMap) {
        EmployeeRequestDto request = new EmployeeRequestDto();
        request.setEmployeeCode(dto.getEmployeeCode());
        request.setEmployeeName(dto.getEmployeeName());
        request.setCommunicationName(dto.getCommunicationName());
        request.setDepartmentId(findKeyByValue(departmentMap, dto.getDepartmentName()));
        request.setDesignationId(findKeyByValue(designationMap, dto.getDesignationName()));
        request.setReportingManager(dto.getReportingManagerCode() != null && !dto.getReportingManagerCode().isBlank() ? Long.valueOf(dto.getReportingManagerCode()) : null);
        request.setEmployeeType(dto.getEmployeeType());
        request.setGender(dto.getGender());
        request.setMaritalStatus(dto.getMaritalStatus());
        request.setSkills(dto.getSkills() != null && !dto.getSkills().isBlank() ? List.of(dto.getSkills().split("\\s*,\\s*")) : List.of());
        request.setLanguages(dto.getLanguages());
        request.setMobile(dto.getMobile());
        request.setAlternateMobile(dto.getAlternateMobile());
        request.setEmail(dto.getEmail());
        request.setAlternateEmail(dto.getAlternateEmail());
        if (dto.getDob() != null && !dto.getDob().isBlank()) {
            request.setDob(LocalDate.parse(dto.getDob()));
        }
        if (dto.getJoiningDate() != null && !dto.getJoiningDate().isBlank()) {
            request.setJoiningDate(LocalDate.parse(dto.getJoiningDate()));
        }
        request.setAddress(dto.getAddress());
        request.setCity(dto.getCity());
        request.setStateId(findKeyByValue(stateMap, dto.getStateName()));
        request.setCountryId(findKeyByValue(countryMap, dto.getCountryName()));
        request.setZipCode(dto.getZipCode());
        request.setBloodGroup(dto.getBloodGroup());
        request.setStatus("Inactive".equalsIgnoreCase(dto.getStatus()));
        return request;
    }

    private Long findKeyByValue(Map<Long, String> map, String value) {
        if (value == null) {
            return null;
        }
        return map.entrySet().stream().filter(e -> e.getValue().equalsIgnoreCase(value)).map(Map.Entry::getKey).findFirst().orElse(null);
    }
}