package common.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EmailRequestDto {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Recipient must be a valid email address")
    private String to;
    @NotBlank(message = "Subject is required")
    private String subject;
    @NotBlank(message = "Body is required")
    private String body;
    private List<AttachmentDto> attachments;
}
