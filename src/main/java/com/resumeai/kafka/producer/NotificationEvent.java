package com.resumeai.kafka.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class NotificationEvent {
    private String recipientEmail;
    private String subject;
    private String body;
    private String notificationType;
    private String referenceId;
    private Instant createdAt;
}
