package com.resumeai.kafka.producer;

import com.resumeai.constants.AppConstants;
import com.resumeai.model.JobPosting;
import com.resumeai.model.MatchResult;
import com.resumeai.model.Resume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

// ── Producer ───────────────────────────────────────────────────

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishResumeUploaded(ResumeUploadedEvent event) {
        send(AppConstants.TOPIC_RESUME_UPLOADED, event.getResumeId(), event);
    }

    public void publishResumeProcessed(ResumeProcessedEvent event) {
        send(AppConstants.TOPIC_RESUME_PROCESSED, event.getResumeId(), event);
    }

    public void publishJobMatched(JobMatchedEvent event) {
        send(AppConstants.TOPIC_JOB_MATCHED, event.getMatchId(), event);
    }

    public void publishNotification(NotificationEvent event) {
        send(AppConstants.TOPIC_NOTIFICATION, event.getRecipientEmail(), event);
    }

    public static ResumeUploadedEvent buildUploadedEvent(String resumeId, String candidateId, String email, String storedFilename, String contentType) {
        return ResumeUploadedEvent.builder().resumeId(resumeId).candidateId(candidateId).candidateEmail(email)
                .storedFilename(storedFilename).contentType(contentType).uploadedAt(Instant.now()).eventType("RESUME_UPLOADED").build();
    }

    public static ResumeProcessedEvent buildProcessedEvent(Resume resume) {
        return ResumeProcessedEvent.builder().resumeId(resume.getId()).candidateId(resume.getCandidateId())
                .technicalSkills(resume.getTechnicalSkills()).softSkills(resume.getSoftSkills())
                .totalExperienceYears(resume.getTotalExperienceYears())
                .processingStatus(resume.getProcessingStatus().name()).processedAt(Instant.now()).eventType("RESUME_PROCESSED").build();
    }

    public static JobMatchedEvent buildJobMatchedEvent(MatchResult match, Resume resume, JobPosting job) {
        return JobMatchedEvent.builder().matchId(match.getId()).resumeId(match.getResumeId())
                .jobId(match.getJobId()).candidateId(match.getCandidateId()).recruiterId(match.getRecruiterId())
                .overallScore(match.getOverallScore())
                .recommendation(match.getRecommendation() != null ? match.getRecommendation().name() : null)
                .matchedAt(Instant.now()).eventType("JOB_MATCHED").build();
    }

    private void send(String topic, String key, Object payload) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, payload);
        future.whenComplete((result, ex) -> {
            if (ex != null) log.error("Kafka send failed topic=[{}]: {}", topic, ex.getMessage());
            else log.debug("Event sent topic=[{}] offset=[{}]", topic, result.getRecordMetadata().offset());
        });
    }
}
