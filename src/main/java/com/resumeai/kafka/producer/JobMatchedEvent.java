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
class JobMatchedEvent {
    private String matchId;
    private String resumeId;
    private String jobId;
    private String candidateId;
    private String recruiterId;
    private Double overallScore;
    private String recommendation;
    private Instant matchedAt;
    private String eventType;
}
