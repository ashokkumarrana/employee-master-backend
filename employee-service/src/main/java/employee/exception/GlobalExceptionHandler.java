package employee.exception;
import employee.dto.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import java.util.stream.Collectors;

//@SuppressWarnings("unused")
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleEmployeeException(EmployeeException ex) {
        return ResponseEntity.status(ex.getHttpStatus()).body(new ApiResponseDto<>(ex.getStatus(), ex.getMessage(), null, ex.getMessage()));
    }

    @ExceptionHandler(InvalidExcelFileException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidExcelFileException(InvalidExcelFileException ex) {
        return ResponseEntity.status(ex.getHttpStatus()).body(new ApiResponseDto<>(ex.getStatus(), ex.getMessage(), null, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream().map(error -> String.valueOf(error.getDefaultMessage())).collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDto<>(HttpStatus.BAD_REQUEST.value(), "Validation failed", null, message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String causeMessage = ex.getMostSpecificCause().getMessage();
        String duplicateValue = extractDuplicateValue(causeMessage);
        String suffix = duplicateValue.isBlank() ? "." : ": " + duplicateValue;
        String message = "Employee already exists" + suffix;
        if (causeMessage != null) {
            String lowerMessage = causeMessage.toLowerCase();
            if (lowerMessage.contains("employee_code")) {
                message = "Employee code already exists" + suffix;
            } else if (lowerMessage.contains("email")) {
                message = "Email already exists" + suffix;
            } else if (lowerMessage.contains("mobile")) {
                message = "Mobile number already exists" + suffix;
            }
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponseDto<>(HttpStatus.CONFLICT.value(), message, null, message));
    }

    private String extractDuplicateValue(String message) {
        if (message == null) return "";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("Duplicate entry '(.*?)'").matcher(message);
        return matcher.find() ? matcher.group(1) : "";
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected server error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", null, "Internal server error"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponseDto<>(HttpStatus.FORBIDDEN.value(), "Access denied", null, "You do not have permission to perform this action."));
    }
}