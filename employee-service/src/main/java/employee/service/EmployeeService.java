package employee.service;
import employee.client.AuthServiceClient;
import employee.client.DropdownClient;
import employee.dto.*;
import employee.entity.Employee;
import employee.entity.EmployeeHistory;
import employee.enums.AttachmentType;
import employee.exception.EmployeeException;
import employee.repository.EmployeeHistoryRepository;
import employee.repository.EmployeeRepository;
import employee.security.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeHistoryRepository employeeHistoryRepository;
    private final FileStorageService fileStorageService;
    private final DropdownClient dropdownClient;
    private final AuthServiceClient authServiceClient;
    private final org.springframework.cache.CacheManager cacheManager;

    @Transactional
    @CacheEvict(value = "employeeById", allEntries = true)
    public ResponseEntity<ApiResponseDto<?>> addOrUpdateEmployees(List<EmployeeRequestDto> requestDto) {
        try {
            log.info("Excel Save Request received. Employee count: {}", Objects.nonNull(requestDto) ? requestDto.size() : 0);
            if (Objects.isNull(requestDto) || requestDto.isEmpty()) {
                throw new EmployeeException(400, "Request cannot be null or empty.", HttpStatus.BAD_REQUEST);
            }
            checkDuplicatesWithinRequest(requestDto);
            requestDto.forEach(this::validateEmployeeRequest);
            List<EmployeeChange> employeeChanges = requestDto.stream().map(this::applyEmployeeChanges).toList();
            employeeRepository.saveAllAndFlush(employeeChanges.stream().map(EmployeeChange::employee).toList());
            employeeChanges.forEach(this::saveEmployeeHistory);
            List<EmployeeResponseDto> responseData = employeeChanges.stream().map(change -> convertToResponseDto(change.employee())).toList();
            boolean allNewEmployees = requestDto.stream().allMatch(dto -> Objects.isNull(dto.getId()));
            HttpStatus httpStatus = allNewEmployees ? HttpStatus.CREATED : HttpStatus.OK;
            String message = allNewEmployees ? "Employees added successfully." : "Employees saved/updated successfully.";
            return ResponseEntity.status(httpStatus).body(new ApiResponseDto<>(httpStatus.value(), message, responseData, "none"));
        } catch (DataIntegrityViolationException ex) {
            log.error("Duplicate employee data found: {}", ex.getMostSpecificCause().getMessage(), ex);
            throw new EmployeeException(409, getDuplicateMessage(ex, requestDto), HttpStatus.CONFLICT);
        }
    }

    private record EmployeeChange(Employee employee, boolean isUpdate) {
    }

    private void validateEmployeeRequest(EmployeeRequestDto dto) {
        if (Objects.isNull(dto))
            throw new EmployeeException(400, "Employee request cannot be null.", HttpStatus.BAD_REQUEST);
        if (Objects.nonNull(dto.getDob()) && dto.getDob().isAfter(LocalDate.now().minusYears(18)))
            throw new EmployeeException(400, "Employee must be at least 18 years old.", HttpStatus.BAD_REQUEST);
        checkDuplicateEmployee(dto);
    }

    private EmployeeChange applyEmployeeChanges(EmployeeRequestDto dto) {
        boolean isUpdate = Objects.nonNull(dto.getId());
        Employee employee = isUpdate ? employeeRepository.findById(dto.getId()).orElseThrow(() -> new EmployeeException(404, "Employee not found.", HttpStatus.NOT_FOUND)) : new Employee();
        BeanUtils.copyProperties(dto, employee, "id", "status");
        if (isUpdate) {
            employee.setStatus(dto.isStatus());
            employee.setUpdatedBy(CurrentUserContext.getUserId());
            employee.setUpdatedAt(LocalDateTime.now());
        }
        return new EmployeeChange(employee, isUpdate);
    }

    private void saveEmployeeHistory(EmployeeChange change) {
        saveHistory(change.employee(), change.isUpdate() ? "UPDATE" : "CREATE");
    }

    private void checkDuplicatesWithinRequest(List<EmployeeRequestDto> requestDto) {
        List<String> duplicateFields = new ArrayList<>();
        findDuplicateValues(requestDto, EmployeeRequestDto::getEmployeeCode).forEach(value -> duplicateFields.add("Duplicate employee code in request: " + value));
        findDuplicateValues(requestDto, EmployeeRequestDto::getEmail).forEach(value -> duplicateFields.add("Duplicate email in request: " + value));
        findDuplicateValues(requestDto, EmployeeRequestDto::getMobile).forEach(value -> duplicateFields.add("Duplicate mobile number in request: " + value));
        if (!duplicateFields.isEmpty()) {
            throw new EmployeeException(409, String.join("\n", duplicateFields), HttpStatus.CONFLICT);
        }
    }

    private Set<String> findDuplicateValues(List<EmployeeRequestDto> requestDto, java.util.function.Function<EmployeeRequestDto, String> fieldExtractor) {
        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new LinkedHashSet<>();
        for (EmployeeRequestDto dto : requestDto) {
            String value = fieldExtractor.apply(dto);
            if (Objects.nonNull(value) && !value.isBlank())
                if (!seen.add(value.trim().toLowerCase())) duplicates.add(value.trim());
        }
        return duplicates;
    }

    private void checkDuplicateEmployee(EmployeeRequestDto requestDto) {
        List<String> duplicateFields = new ArrayList<>();
        if (employeeRepository.existsDuplicateEmployeeCode(requestDto.getEmployeeCode(), requestDto.getId()))
            duplicateFields.add("Employee code already exists: " + requestDto.getEmployeeCode());
        if (employeeRepository.existsDuplicateEmployeeEmail(requestDto.getEmail(), requestDto.getId()))
            duplicateFields.add("Email already exists: " + requestDto.getEmail());
        if (employeeRepository.existsDuplicateEmployeeMobile(requestDto.getMobile(), requestDto.getId()))
            duplicateFields.add("Mobile number already exists: " + requestDto.getMobile());
        if (!duplicateFields.isEmpty())
            throw new EmployeeException(409, String.join("\n", duplicateFields), HttpStatus.CONFLICT);
    }

    private String getDuplicateMessage(DataIntegrityViolationException ex, List<EmployeeRequestDto> requestDto) {
        String message = ex.getMostSpecificCause().getMessage();
        if (Objects.isNull(message)) {
            return "Employee already exists.";
        }
        String duplicateValue = extractDuplicateValue(message);
        if (!duplicateValue.isBlank() && Objects.nonNull(requestDto)) {
            for (EmployeeRequestDto dto : requestDto) {
                if (duplicateValue.equalsIgnoreCase(dto.getEmployeeCode())) {
                    return "Employee code already exists: " + duplicateValue;
                }
                if (duplicateValue.equalsIgnoreCase(dto.getEmail())) {
                    return "Email already exists: " + duplicateValue;
                }
                if (duplicateValue.equals(dto.getMobile())) {
                    return "Mobile number already exists: " + duplicateValue;
                }
                if (duplicateValue.equals(dto.getAlternateMobile())) {
                    return "Alternate mobile number already exists: " + duplicateValue;
                }
                if (duplicateValue.equalsIgnoreCase(dto.getAlternateEmail())) {
                    return "Alternate email already exists: " + duplicateValue;
                }
            }
        }
        return "Employee already exists.";
    }

    private String extractDuplicateValue(String message) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("Duplicate entry '(.*?)'").matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public ResponseEntity<ApiResponseDto<?>> getAllEmployees(int page, int size, String sortBy, String direction, EmployeeFilterRequestDto filter) {
        Set<String> validSortFields = Set.of("id", "employeeCode", "employeeName", "mobile", "email", "joiningDate");
        if (!validSortFields.contains(sortBy)) {
            sortBy = "id";
        }
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        String city = Objects.nonNull(filter) && Objects.nonNull(filter.getCity()) ? filter.getCity().trim() : null;
        if (Objects.nonNull(filter) && Objects.nonNull(filter.getFromDate()) && Objects.nonNull(filter.getToDate()) && filter.getFromDate().isAfter(filter.getToDate())) {
            throw new EmployeeException(400, "From date cannot be greater than To date.", HttpStatus.BAD_REQUEST);
        }
        Page<EmployeeResponseDto> data = employeeRepository.findEmployees(Objects.nonNull(filter) ? filter.getDepartmentId() : null, Objects.nonNull(filter) ? filter.getDesignationId() : null, city, Objects.nonNull(filter) ? filter.getStatus() : null, Objects.nonNull(filter) ? filter.getFromDate() : null, Objects.nonNull(filter) ? filter.getToDate() : null, pageable).map(this::convertToResponseDto);
        return ResponseEntity.ok(new ApiResponseDto<>(200, "Employees fetched successfully.", data, null));
    }

    @Cacheable(value = "employeeById", key = "#id")
    public ApiResponseDto<?> getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeException(404, "Employee not found.", HttpStatus.NOT_FOUND));
        EmployeeResponseDto data = convertToResponseDto(employee);
        return new ApiResponseDto<>(HttpStatus.OK.value(), "Employee fetched successfully", data, "none");
    }

    @Transactional
    @CacheEvict(value = "employeeById", key = "#id")
    public ResponseEntity<ApiResponseDto<?>> deleteEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeException(404, "Employee not found with id: " + id, HttpStatus.NOT_FOUND));
        boolean wasActive = !employee.isStatus();
        employee.setStatus(true);
        employeeRepository.save(employee);
        if (wasActive) {
            saveHistory(employee, "DELETE");
        }
        return ResponseEntity.ok(new ApiResponseDto<>(HttpStatus.OK.value(), "Employee deactivated successfully", "none", "none"));
    }

    public ApiResponseDto<Map<String, Long>> getEmployeeCounts(LocalDate fromDate, LocalDate toDate) {
        if (Objects.nonNull(fromDate) && Objects.nonNull(toDate) && fromDate.isAfter(toDate)) {
            throw new EmployeeException(400, "From date cannot be greater than To date.", HttpStatus.BAD_REQUEST);
        }
        long totalEmployees;
        long activeEmployees;
        long inactiveEmployees;

        if (Objects.nonNull(fromDate) || Objects.nonNull(toDate)) {
            List<Object[]> result = employeeRepository.getEmployeeCountsByDateRange(fromDate, toDate);
            Object[] row = result.isEmpty() ? new Object[]{0L, 0L, 0L} : result.getFirst();
            totalEmployees = Objects.nonNull(row[0]) ? ((Number) row[0]).longValue() : 0L;
            activeEmployees = Objects.nonNull(row[1]) ? ((Number) row[1]).longValue() : 0L;
            inactiveEmployees = Objects.nonNull(row[2]) ? ((Number) row[2]).longValue() : 0L;
        } else {
            activeEmployees = employeeRepository.countByStatusFalse();
            inactiveEmployees = employeeRepository.countByStatusTrue();
            totalEmployees = activeEmployees + inactiveEmployees;
        }

        Map<String, Long> counts = new HashMap<>();
        counts.put("totalEmployees", totalEmployees);
        counts.put("activeEmployees", activeEmployees);
        counts.put("inactiveEmployees", inactiveEmployees);
        return new ApiResponseDto<>(HttpStatus.OK.value(), "Employee counts fetched successfully", counts, "none");
    }

    public ApiResponseDto<List<EmployeeResponseDto>> search(EmployeeSearchRequestDto requestDto) {
        if (Objects.isNull(requestDto))
            throw new EmployeeException(400, "Search request cannot be null", HttpStatus.BAD_REQUEST);
        String search = Objects.nonNull(requestDto.getSearch()) ? requestDto.getSearch().trim() : "";
        if (search.isEmpty()) throw new EmployeeException(400, "Search value cannot be empty", HttpStatus.BAD_REQUEST);
        if (search.length() < 2)
            throw new EmployeeException(400, "Search value must contain at least 2 characters", HttpStatus.BAD_REQUEST);
        List<EmployeeResponseDto> data = employeeRepository.searchEmployees(search, PageRequest.of(0, 10)).getContent().stream().map(this::convertToResponseDto).toList();
        return new ApiResponseDto<>(HttpStatus.OK.value(), "Employees searched successfully", data, "none");
    }

    public ApiResponseDto<List<DepartmentEmployeeCountDto>> getDepartmentEmployeeCounts(LocalDate fromDate, LocalDate toDate, Boolean status) {
        if (Objects.nonNull(fromDate) && Objects.nonNull(toDate) && fromDate.isAfter(toDate)) {
            throw new EmployeeException(400, "From date cannot be greater than To date.", HttpStatus.BAD_REQUEST);
        }
        List<Object[]> results = employeeRepository.getDepartmentEmployeeCounts(fromDate, toDate, status);
        List<DropdownClient.DepartmentDto> departments = dropdownClient.getDepartments();
        Map<Long, String> departmentNames = departments.stream().collect(Collectors.toMap(DropdownClient.DepartmentDto::getId, DropdownClient.DepartmentDto::getName));
        List<DepartmentEmployeeCountDto> data = results.stream().map(row -> new DepartmentEmployeeCountDto(((Number) row[0]).longValue(), departmentNames.get(((Number) row[0]).longValue()), ((Number) row[1]).longValue(), ((Number) row[2]).longValue(), ((Number) row[3]).longValue())).toList();
        return new ApiResponseDto<>(HttpStatus.OK.value(), "Department-wise employee count fetched successfully", data, "none");
    }

    // Employee History
    private void saveHistory(Employee employee, String action) {
        EmployeeHistory history = new EmployeeHistory();
        history.setEmployeeId(employee.getId());
        history.setAction(action);
        history.setEmployeeCode(employee.getEmployeeCode());
        history.setEmployeeName(employee.getEmployeeName());
        history.setCommunicationName(employee.getCommunicationName());
        history.setDepartmentId(employee.getDepartmentId());
        history.setDesignationId(employee.getDesignationId());
        history.setReportingManager(employee.getReportingManager());
        history.setEmployeeType(employee.getEmployeeType());
        history.setGender(employee.getGender());
        history.setMaritalStatus(employee.getMaritalStatus());
        history.setSkills(employee.getSkills());
        history.setLanguages(employee.getLanguages());
        history.setMobile(employee.getMobile());
        history.setAlternateMobile(employee.getAlternateMobile());
        history.setEmail(employee.getEmail());
        history.setAlternateEmail(employee.getAlternateEmail());
        history.setDob(employee.getDob());
        history.setJoiningDate(employee.getJoiningDate());
        history.setAddress(employee.getAddress());
        history.setCity(employee.getCity());
        history.setStateId(employee.getStateId());
        history.setCountryId(employee.getCountryId());
        history.setZipCode(employee.getZipCode());
        history.setBloodGroup(employee.getBloodGroup());
        history.setStatus(employee.isStatus());
        history.setProfileImage(employee.getProfileImage());
        history.setDocuments(employee.getDocuments());
        history.setRemarks(employee.getRemarks());
        history.setCreatedBy(employee.getCreatedBy());
        history.setCreatedAt(employee.getCreatedAt());
        history.setUpdatedBy(employee.getUpdatedBy());
        history.setUpdatedAt(employee.getUpdatedAt());
        if (!"CREATE".equals(action)) {
            history.setChangedBy(CurrentUserContext.getUserId());
            history.setChangedAt(LocalDateTime.now());
        }
        employeeHistoryRepository.save(history);
        evictEmployeeCache(employee.getId());
    }

    public ApiResponseDto<?> getEmployeeHistory(Long employeeId, int page, int size, String sortBy, String direction) {
        Set<String> validSortFields = Set.of("id", "employeeId", "employeeCode", "changedAt");
        if (!validSortFields.contains(sortBy)) {
            sortBy = "changedAt";
        }
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<EmployeeHistoryResponseDto> history = employeeHistoryRepository.findByEmployeeId(employeeId, pageable).map(this::convertToHistoryResponseDto);
        return new ApiResponseDto<>(HttpStatus.OK.value(), "Employee history fetched successfully", history, "none");
    }

    private EmployeeHistoryResponseDto convertToHistoryResponseDto(EmployeeHistory history) {
        EmployeeHistoryResponseDto responseDto = new EmployeeHistoryResponseDto();
        BeanUtils.copyProperties(history, responseDto);
        responseDto.setCreatedByName(getUserName(history.getCreatedBy()));
        responseDto.setChangedByName(getUserName(history.getChangedBy()));
        return responseDto;
    }

    private void evictEmployeeCache(Long employeeId) {
        Objects.requireNonNull(cacheManager.getCache("employeeById")).evict(employeeId);
        Objects.requireNonNull(cacheManager.getCache("employeeHistory")).evict(employeeId);
    }

    private EmployeeResponseDto convertToResponseDto(Employee employee) {
        EmployeeResponseDto responseDto = new EmployeeResponseDto();
        BeanUtils.copyProperties(employee, responseDto);
        responseDto.setCreatedByName(getUserName(employee.getCreatedBy()));
        responseDto.setUpdatedByName(getUserName(employee.getUpdatedBy()));
        return responseDto;
    }

    private String getUserName(Long userId) {
        if (Objects.isNull(userId)) return null;
        try {
            UserResponseDto user = authServiceClient.getUserById(userId);
            return Objects.nonNull(user) ? user.getName() : null;
        } catch (Exception ex) {
            log.warn("Unable to fetch user: {}", userId, ex);
            return null;
        }
    }

    public ResponseEntity<ApiResponseDto<?>> uploadAttachments(MultiValueMap<String, MultipartFile> files) {
        if (files == null || files.isEmpty())
            throw new EmployeeException(400, "Attachments are required", HttpStatus.BAD_REQUEST);
        List<String> errors = new ArrayList<>();
        List<Long> updatedEmployeeIds = new ArrayList<>();
        for (Map.Entry<String, List<MultipartFile>> entry : files.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue() == null || entry.getValue().isEmpty()) continue;
            MultipartFile file = entry.getValue().getFirst();
            try {
                if (key.startsWith("profileImage_")) {
                    Long employeeId = Long.parseLong(key.substring("profileImage_".length()));
                    Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new EmployeeException(404, "Employee not found: " + employeeId, HttpStatus.NOT_FOUND));
                    String path = fileStorageService.store(file, AttachmentType.PROFILE_IMAGE);
                    employee.setProfileImage(path);
                    employeeRepository.save(employee);
                    updatedEmployeeIds.add(employeeId);
                    evictEmployeeCache(employeeId);
                } else if (key.startsWith("documents_")) {
                    Long employeeId = Long.parseLong(key.substring("documents_".length()));
                    Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new EmployeeException(404, "Employee not found: " + employeeId, HttpStatus.NOT_FOUND));
                    String path = fileStorageService.store(file, AttachmentType.DOCUMENTS);
                    employee.setDocuments(path);
                    employeeRepository.save(employee);
                    updatedEmployeeIds.add(employeeId);
                    evictEmployeeCache(employeeId);
                }
            } catch (EmployeeException ex) {
                errors.add("Key '" + key + "': " + ex.getMessage());
            } catch (NumberFormatException ex) {
                errors.add("Invalid employee id in key: " + key);
            }
        }
        if (!errors.isEmpty() && updatedEmployeeIds.isEmpty()) {
            throw new EmployeeException(400, String.join("\n", errors), HttpStatus.BAD_REQUEST);
        }
        String message = errors.isEmpty() ? "Attachments uploaded successfully." : "Attachments uploaded with some errors:\n" + String.join("\n", errors);
        return ResponseEntity.ok(new ApiResponseDto<>(HttpStatus.OK.value(), message, updatedEmployeeIds, "none"));
    }

}