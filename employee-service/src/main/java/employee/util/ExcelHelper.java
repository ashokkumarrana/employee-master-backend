package employee.util;
import common.util.EmployeeExcelHeaders;
import employee.dto.EmployeeExcelDto;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ExcelHelper {

    public static EmployeeExcelDto fromRow(Map<String, String> row, int fallbackRowNumber) {
        EmployeeExcelDto dto = new EmployeeExcelDto();
        String rowNumber = row.get("_rowNumber");
        dto.setRowNumber(rowNumber != null ? Integer.parseInt(rowNumber) : fallbackRowNumber);
        dto.setEmployeeCode(row.get("Employee Code"));
        dto.setEmployeeName(row.get("Employee Name"));
        dto.setCommunicationName(row.get("Communication Name"));
        dto.setDepartmentName(row.get("Department Name"));
        dto.setDesignationName(row.get("Designation Name"));
        dto.setReportingManagerCode(row.get("Reporting Manager Code"));
        dto.setEmployeeType(row.get("Employee Type"));
        dto.setGender(row.get("Gender"));
        dto.setMaritalStatus(row.get("Marital Status"));
        dto.setSkills(row.get("Skills"));
        dto.setLanguages(row.get("Languages"));
        dto.setMobile(row.get("Mobile"));
        dto.setAlternateMobile(row.get("Alternate Mobile"));
        dto.setEmail(row.get("Email"));
        dto.setAlternateEmail(row.get("Alternate Email"));
        dto.setDob(row.get("DOB"));
        dto.setJoiningDate(row.get("Joining Date"));
        dto.setAddress(row.get("Address"));
        dto.setCity(row.get("City"));
        dto.setStateName(row.get("State Name"));
        dto.setCountryName(row.get("Country Name"));
        dto.setZipCode(row.get("Zip Code"));
        dto.setBloodGroup(row.get("Blood Group"));
        dto.setStatus(row.get("Status"));
        return dto;
    }

    public static Map<String, String> toRow(EmployeeExcelDto e) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("Employee Code", safe(e.getEmployeeCode()));
        row.put("Employee Name", safe(e.getEmployeeName()));
        row.put("Communication Name", safe(e.getCommunicationName()));
        row.put("Department Name", safe(e.getDepartmentName()));
        row.put("Designation Name", safe(e.getDesignationName()));
        row.put("Reporting Manager Code", safe(e.getReportingManagerCode()));
        row.put("Employee Type", safe(e.getEmployeeType()));
        row.put("Gender", safe(e.getGender()));
        row.put("Marital Status", safe(e.getMaritalStatus()));
        row.put("Skills", safe(e.getSkills()));
        row.put("Languages", safe(e.getLanguages()));
        row.put("Mobile", safe(e.getMobile()));
        row.put("Alternate Mobile", safe(e.getAlternateMobile()));
        row.put("Email", safe(e.getEmail()));
        row.put("Alternate Email", safe(e.getAlternateEmail()));
        row.put("DOB", safe(e.getDob()));
        row.put("Joining Date", safe(e.getJoiningDate()));
        row.put("Address", safe(e.getAddress()));
        row.put("City", safe(e.getCity()));
        row.put("State Name", safe(e.getStateName()));
        row.put("Country Name", safe(e.getCountryName()));
        row.put("Zip Code", safe(e.getZipCode()));
        row.put("Blood Group", safe(e.getBloodGroup()));
        row.put("Status", safe(e.getStatus()));
        return row;
    }

    public static Map<String, String> toDownloadRow(EmployeeExcelDto e) {
        Map<String, String> row = toRow(e);
        row.put("Remarks", safe(e.getRemarks()));
        row.put("Created By", safe(e.getCreatedBy()));
        row.put("Created At", safe(e.getCreatedAt()));
        row.put("Updated By", safe(e.getUpdatedBy()));
        row.put("Updated At", safe(e.getUpdatedAt()));
        return row;
    }

    public static Map<String, String> toImportResultRow(EmployeeExcelDto e, String importStatus, String reason) {
        Map<String, String> row = toRow(e);
        row.put(EmployeeExcelHeaders.IMPORT_STATUS_HEADER, importStatus);
        row.put(EmployeeExcelHeaders.IMPORT_REASON_HEADER, safe(reason));
        return row;
    }

    public static List<Map<String, String>> toRows(List<EmployeeExcelDto> list) {
        return list.stream().map(ExcelHelper::toRow).collect(Collectors.toList());
    }

    public static List<Map<String, String>> toDownloadRows(List<EmployeeExcelDto> list) {
        return list.stream().map(ExcelHelper::toDownloadRow).collect(Collectors.toList());
    }

    public static List<Map<String, String>> toImportResultRows(List<EmployeeExcelDto> saved, List<EmployeeExcelDto> invalid, List<EmployeeExcelDto> duplicate) {
        List<Map.Entry<Integer, Map<String, String>>> entries = new ArrayList<>();
        addImportResultEntries(entries, saved, EmployeeExcelHeaders.IMPORT_STATUS_SAVED, true);
        addImportResultEntries(entries, invalid, EmployeeExcelHeaders.IMPORT_STATUS_INVALID, false);
        addImportResultEntries(entries, duplicate, EmployeeExcelHeaders.IMPORT_STATUS_DUPLICATE, false);
        entries.sort(Map.Entry.comparingByKey());
        return entries.stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    private static void addImportResultEntries(List<Map.Entry<Integer, Map<String, String>>> entries, List<EmployeeExcelDto> list, String importStatus, boolean saved) {
        if (list == null) {
            return;
        }
        for (EmployeeExcelDto e : list) {
            if (e == null) {
                continue;
            }
            String reason = saved ? EmployeeExcelHeaders.IMPORT_SAVED_REASON : e.getErrorMessage();
            entries.add(Map.entry(e.getRowNumber(), toImportResultRow(e, importStatus, reason)));
        }
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    public static EmployeeExcelDto createDummyEmployeeData() {
        EmployeeExcelDto employee = new EmployeeExcelDto();
        employee.setEmployeeCode("EMP001");
        employee.setEmployeeName("Rahul Sharma");
        employee.setCommunicationName("Rahul");
        employee.setDepartmentName("IT");
        employee.setDesignationName("Software Engineer");
        employee.setReportingManagerCode("1");
        employee.setEmployeeType("Permanent");
        employee.setGender("Male");
        employee.setMaritalStatus("Married");
        employee.setSkills("Java, Spring Boot");
        employee.setLanguages("Hindi, English");
        employee.setMobile("9876543210");
        employee.setAlternateMobile("9876543211");
        employee.setEmail("rahul.sharma@example.com");
        employee.setAlternateEmail("rahul.alt@example.com");
        employee.setDob("1998-05-15");
        employee.setJoiningDate("2024-01-10");
        employee.setAddress("Ram Nagar");
        employee.setCity("Delhi");
        employee.setStateName("Delhi");
        employee.setCountryName("India");
        employee.setZipCode("110001");
        employee.setBloodGroup("O+");
        employee.setStatus("Active");
        return employee;
    }
}
