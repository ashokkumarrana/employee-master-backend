package employee.dto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EmailRequestDto {
    private String to;
    private String subject;
    private String body;
    private List<AttachmentDto> attachments;
}
