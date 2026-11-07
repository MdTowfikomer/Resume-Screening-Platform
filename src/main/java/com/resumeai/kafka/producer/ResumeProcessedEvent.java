package com.resumeai.kafka.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ResumeProcessedEvent {
    private String resumeId;
    private String candidateId;
    private List<String> technicalSkills;
    private List<String> softSkills;
    private Double totalExperienceYears;
    private String processingStatus;
    private Instant processedAt;
    private String eventType;
}
