package dropdown.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DropdownException extends RuntimeException {

    private final int status;
    private final HttpStatus httpStatus;

    public DropdownException(int status, String message, HttpStatus httpStatus) {
        super(message);
        this.status = status;
        this.httpStatus = httpStatus;
    }
}