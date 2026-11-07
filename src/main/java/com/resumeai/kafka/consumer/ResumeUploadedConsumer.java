package com.resumeai.kafka.consumer;

import com.resumeai.constants.AppConstants;
import com.resumeai.kafka.producer.ResumeEventProducer;
import com.resumeai.model.Resume;
import com.resumeai.repository.mongo.ResumeRepository;
import com.resumeai.service.impl.AiNlpService;
import com.resumeai.util.FileStorageUtil;
import com.resumeai.util.ResumeParserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

/**
 * Kafka consumer that drives the async resume processing pipeline.
 * <p>
 * Flow:
 * 1. ResumeUploadedConsumer → parses PDF, extracts text
 * 2. Calls AI/NLP service for structured skill extraction
 * 3. Saves enriched resume back to MongoDB
 * 4. Publishes ResumeProcessedEvent → triggers matching
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeUploadedConsumer {

    private final ResumeRepository resumeRepository;
    private final FileStorageUtil fileStorageUtil;
    private final ResumeParserUtil resumeParserUtil;
    private final AiNlpService aiNlpService;
    private final ResumeEventProducer eventProducer;

    @KafkaListener(
            topics = AppConstants.TOPIC_RESUME_UPLOADED,
            groupId = AppConstants.CONSUMER_GROUP_MAIN,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeResumeUploaded(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        String resumeId = (String) event.get("resumeId");
        log.info("Processing resume [{}] from topic=[{}] partition=[{}] offset=[{}]",
                resumeId, topic, partition, offset);

        try {
            Resume resume = resumeRepository.findById(resumeId)
                    .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + resumeId));

            resume.setProcessingStatus(Resume.ProcessingStatus.PROCESSING);
            resumeRepository.save(resume);

            // Step 1: PDF Text Extraction
            String extractedText;
            try (InputStream stream = fileStorageUtil.getFileInputStream(resume.getStoredFilename())) {
                extractedText = resumeParserUtil.extractTextFromPdf(stream);
            }
            resume.setRawText(extractedText);

            // Step 2: AI/NLP Enrichment (with rule-based fallback)
            try {
                var aiResult = aiNlpService.extractSkills(extractedText);
                resume.setTechnicalSkills(aiResult.getTechnicalSkills());
                resume.setSoftSkills(aiResult.getSoftSkills());
                resume.setCertifications(aiResult.getCertifications());
                resume.setSummary(aiResult.getSummary());
                resume.setTotalExperienceYears(aiResult.getExperienceYears());
                resume.setAiConfidenceScore(aiResult.getConfidenceScore());
                resume.setWorkExperience(aiResult.getWorkExperience());
                resume.setEducation(aiResult.getEducation());
            } catch (Exception aiEx) {
                log.warn("AI/NLP service unavailable, using rule-based fallback: {}", aiEx.getMessage());
                resume.setTechnicalSkills(resumeParserUtil.extractTechnicalSkills(extractedText));
                resume.setSoftSkills(resumeParserUtil.extractSoftSkills(extractedText));
                resume.setTotalExperienceYears(resumeParserUtil.extractExperienceYears(extractedText));
            }

            // Step 3: Enrich basic contact info if missing
            if (resume.getCandidateEmail() == null) {
                resume.setCandidateEmail(resumeParserUtil.extractEmail(extractedText));
            }
            if (resume.getPhone() == null) {
                resume.setPhone(resumeParserUtil.extractPhone(extractedText));
            }
            if (resume.getLinkedinUrl() == null) {
                resume.setLinkedinUrl(resumeParserUtil.extractLinkedIn(extractedText));
            }

            resume.setProcessingStatus(Resume.ProcessingStatus.COMPLETED);
            resumeRepository.save(resume);

            log.info("Resume [{}] processed successfully. Skills found: {}",
                    resumeId, resume.getTechnicalSkills().size());

            // Step 4: Publish processed event to trigger matching
            eventProducer.publishResumeProcessed(
                    com.resumeai.kafka.producer.ResumeEventProducer.buildProcessedEvent(resume));

            ack.acknowledge();

        } catch (Exception ex) {
            log.error("Failed to process resume [{}]: {}", resumeId, ex.getMessage(), ex);
            // Mark as failed and acknowledge to avoid infinite retry loop
            resumeRepository.findById(resumeId).ifPresent(r -> {
                r.setProcessingStatus(Resume.ProcessingStatus.FAILED);
                r.setProcessingError(ex.getMessage());
                resumeRepository.save(r);
            });
            ack.acknowledge();
        }
    }
}
