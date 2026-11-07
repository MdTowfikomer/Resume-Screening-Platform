package com.resumeai.kafka.producer;// ── Event Payloads ─────────────────────────────────────────────

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ResumeUploadedEvent {
    private String resumeId;
    private String candidateId;
    private String candidateEmail;
    private String storedFilename;
    private String contentType;
    private Instant uploadedAt;
    private String eventType;
}