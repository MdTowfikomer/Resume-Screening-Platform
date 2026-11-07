package com.resumeai.kafka.consumer;

import com.resumeai.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

// ── Notification Consumer ─────────────────────────────────────
@Component
@RequiredArgsConstructor
@Slf4j
class NotificationConsumer {

    @KafkaListener(
            topics = AppConstants.TOPIC_NOTIFICATION,
            groupId = AppConstants.CONSUMER_GROUP_MAIN + "-notifications"
    )
    public void consumeNotification(
            @Payload Map<String, Object> event,
            Acknowledgment ack) {

        String recipient = (String) event.get("recipientEmail");
        String subject = (String) event.get("subject");

        // In production: integrate with SendGrid / AWS SES / SMTP
        log.info("📧 [NOTIFICATION] To: {} | Subject: {}", recipient, subject);
        ack.acknowledge();
    }
}
