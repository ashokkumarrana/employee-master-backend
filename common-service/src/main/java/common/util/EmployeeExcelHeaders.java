package common.util;
import java.util.ArrayList;
import java.util.List;

public final class EmployeeExcelHeaders {

    public static final List<String> HEADERS = List.of("Employee Code", "Employee Name", "Communication Name", "Department Name", "Designation Name", "Reporting Manager Code", "Employee Type", "Gender", "Marital Status", "Skills", "Languages", "Mobile", "Alternate Mobile", "Email", "Alternate Email", "DOB", "Joining Date", "Address", "City", "State Name", "Country Name", "Zip Code", "Blood Group", "Status");
    public static final List<String> FIELD_TYPES = List.of("Required", "Required", "Optional", "Required", "Required", "Optional", "Required", "Required", "Optional", "Optional", "Optional", "Required", "Optional", "Required", "Optional", "Optional", "Required", "Optional", "Optional", "Optional", "Required", "Optional", "Optional", "Optional");
    public static final List<String> DOWNLOAD_HEADERS = concat(HEADERS, "Remarks", "Created By", "Created At", "Updated By", "Updated At");
    public static final List<String> DOWNLOAD_FIELD_TYPES = concat(FIELD_TYPES, "Optional", "Optional", "Optional", "Optional", "Optional");
    public static final String IMPORT_STATUS_HEADER = "Import Status";
    public static final String IMPORT_REASON_HEADER = "Reason";
    public static final String IMPORT_STATUS_SAVED = "Saved";
    public static final String IMPORT_STATUS_INVALID = "Invalid";
    public static final String IMPORT_STATUS_DUPLICATE = "Duplicate";
    public static final String IMPORT_SAVED_REASON = "Saved successfully";
    public static final List<String> IMPORT_RESULT_HEADERS = concat(HEADERS, IMPORT_STATUS_HEADER, IMPORT_REASON_HEADER);

    private static List<String> concat(List<String> base, String... extra) {
        List<String> result = new ArrayList<>(base);
        result.addAll(List.of(extra));
        return result;
    }
}