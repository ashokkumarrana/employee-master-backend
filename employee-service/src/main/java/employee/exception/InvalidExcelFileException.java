package employee.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidExcelFileException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final int status;

    public InvalidExcelFileException(String message) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.status = HttpStatus.BAD_REQUEST.value();
    }
}
