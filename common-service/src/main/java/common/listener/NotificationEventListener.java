package common.listener;

import common.event.NotificationEvent;
import common.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final EmailService emailService;

    @Async
    @EventListener
    public void handleNotification(NotificationEvent event) {
        log.info("Sending notification email to: {}", event.to());
        emailService.sendEmail(event.to(), event.subject(), event.body(), event.attachments());
    }
}
