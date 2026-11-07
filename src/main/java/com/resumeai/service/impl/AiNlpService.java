package com.resumeai.service.impl;

import com.resumeai.model.Resume;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * HTTP client to the Python AI/NLP microservice.
 * Falls back gracefully to rule-based extraction if service is down.
 */
@Service
@Slf4j
public class AiNlpService {

    private final RestTemplate restTemplate;

    @Value("${app.ai-nlp.base-url:http://localhost:5000}")
    private String baseUrl;

    @Value("${app.ai-nlp.enabled:true}")
    private boolean enabled;

    public AiNlpService(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

    public NlpResult extractSkills(String resumeText) {
        if (!enabled) {
            throw new AiNlpUnavailableException("AI NLP service is disabled");
        }

        var request = Map.of("text", resumeText);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<NlpApiResponse> response = restTemplate.postForEntity(
                    baseUrl + "/api/v1/extract",
                    new HttpEntity<>(request, headers),
                    NlpApiResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return mapToResult(response.getBody());
            }
            throw new AiNlpUnavailableException("AI NLP service returned: " + response.getStatusCode());

        } catch (Exception ex) {
            log.warn("AI NLP service call failed: {}", ex.getMessage());
            throw new AiNlpUnavailableException("AI NLP service unavailable: " + ex.getMessage());
        }
    }

    public double computeMatchScore(String resumeText, List<String> requiredSkills,
                                    List<String> preferredSkills) {
        if (!enabled) return -1;

        var request = Map.of(
                "resume_text",      resumeText,
                "required_skills",  requiredSkills,
                "preferred_skills", preferredSkills
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/api/v1/match-score",
                    new HttpEntity<>(request, new HttpHeaders()),
                    Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return ((Number) response.getBody().get("score")).doubleValue();
            }
        } catch (Exception ex) {
            log.warn("AI match score call failed: {}", ex.getMessage());
        }
        return -1;
    }

    private NlpResult mapToResult(NlpApiResponse response) {
        return NlpResult.builder()
                .technicalSkills(response.getTechnicalSkills())
                .softSkills(response.getSoftSkills())
                .certifications(response.getCertifications())
                .summary(response.getSummary())
                .experienceYears(response.getExperienceYears())
                .confidenceScore(response.getConfidenceScore())
                .workExperience(response.getWorkExperience())
                .education(response.getEducation())
                .build();
    }

    // ── Inner types ────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NlpResult {
        private List<String>                technicalSkills;
        private List<String>                softSkills;
        private List<String>                certifications;
        private String                      summary;
        private Double                      experienceYears;
        private Double                      confidenceScore;
        private List<Resume.WorkExperience> workExperience;
        private List<Resume.Education>      education;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class NlpApiResponse {
        private List<String>                technical_skills;
        private List<String>                soft_skills;
        private List<String>                certifications;
        private String                      summary;
        private Double                      experience_years;
        private Double                      confidence_score;
        private List<Resume.WorkExperience> work_experience;
        private List<Resume.Education>      education;

        public List<String> getTechnicalSkills() { return technical_skills; }
        public List<String> getSoftSkills()       { return soft_skills; }
        public Double       getExperienceYears()  { return experience_years; }
        public Double       getConfidenceScore()  { return confidence_score; }
        public List<Resume.WorkExperience> getWorkExperience() { return work_experience; }
    }

    static class AiNlpUnavailableException extends RuntimeException {
        public AiNlpUnavailableException(String message) { super(message); }
    }
}
