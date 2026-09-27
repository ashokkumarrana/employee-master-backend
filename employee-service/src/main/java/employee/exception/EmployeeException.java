package employee.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EmployeeException extends RuntimeException {
    private final int status;
    private final HttpStatus httpStatus;

    public EmployeeException(int status, String message, HttpStatus httpStatus) {
        super(message);
        this.status = status;
        this.httpStatus = httpStatus;
    }
}