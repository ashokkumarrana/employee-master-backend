package common.event;
import common.dto.AttachmentDto;
import java.util.List;

public record NotificationEvent(
        String to,
        String subject,
        String body,
        List<AttachmentDto> attachments
) {
}
