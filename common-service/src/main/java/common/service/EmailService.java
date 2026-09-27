package common.service;
import common.dto.AttachmentDto;
import common.dto.EmailRequestDto;
import common.event.NotificationEvent;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${app.mail.from}")
    private String fromAddress;

    public void sendAsync(EmailRequestDto request) {
        eventPublisher.publishEvent(new NotificationEvent(request.getTo(), request.getSubject(), request.getBody(), request.getAttachments()));
    }

    public void sendImportSummaryAsync(String to, int savedCount, int invalidCount, int duplicateCount, String attachmentName, byte[] attachmentContent) {
        List<AttachmentDto> attachments = attachmentContent == null ? null : List.of(new AttachmentDto(attachmentName, attachmentContent));
        eventPublisher.publishEvent(new NotificationEvent(to, "Employee Excel Import Completed", buildImportSummaryBody(savedCount, invalidCount, duplicateCount), attachments));
    }

    public void sendExcelReportAsync(String to, String subject, String fileName, byte[] fileContent) {
        List<AttachmentDto> attachments = fileContent == null ? null : List.of(new AttachmentDto(fileName, fileContent));
        eventPublisher.publishEvent( new NotificationEvent( to, subject, buildExcelReportBody(), attachments ) );
    }
    private String buildExcelReportBody(){ return "Hello,\n\n"
            + "Your employee Excel report is ready.\n\n"
            + "The selected date range was more than 7 days, so the report could not be downloaded directly. "
            + "As requested, the complete employee report has been generated and is attached to this email.\n\n"
            + "Report Details\n" + "--------------\n" + "• Report Type : Employee Excel Report\n"
            + "• Date Range : More than 7 days\n" + "• File : employees.xlsx\n\n"
            + "Please download the attached Excel file to view the employee records.\n\n"
            + "Regards,\n"
            + "Employee Master Team";
    }
    public String buildImportSummaryBody(int savedCount, int invalidCount, int duplicateCount) {
        int total = savedCount + invalidCount + duplicateCount;
        return "Hello,\n\n"
                + "The employee Excel import has been completed.\n\n"
                + "Import Summary\n"
                + "--------------\n"
                + "Total Records      : " + total + "\n"
                + "Successfully Saved : " + savedCount + "\n"
                + "Invalid Records    : " + invalidCount + "\n"
                + "Duplicate Records  : " + duplicateCount + "\n\n"
                + "All records (Saved, Invalid and Duplicate) are attached with this email.\n"
                + "Please check the 'Import Status' and 'Reason' columns for the result of each record.\n\n"
                + "Regards,\n"
                + "Employee Master Team";
    }

    public void sendEmail(String to, String subject, String body, List<AttachmentDto> attachments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            if (attachments != null) {
                for (AttachmentDto attachment : attachments) {
                    if (attachment != null && attachment.getContent() != null && attachment.getContent().length > 0) {
                        helper.addAttachment(attachment.getFileName() != null ? attachment.getFileName() : "attachment.xlsx", new ByteArrayResource(attachment.getContent()));
                    }
                }
            }
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception ex) {
            log.error("Failed to send email to: {}", to, ex);
        }
    }
}
