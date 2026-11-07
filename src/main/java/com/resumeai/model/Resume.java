package com.resumeai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * MongoDB document representing a candidate's uploaded resume.
 * Stores both raw extracted text and structured AI-parsed data.
 */
@Document(collection = "resumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    private String id;

    // ── Candidate Info ─────────────────────────────────────
    @Indexed
    @Field("candidate_id")
    private String candidateId;

    @Field("candidate_name")
    private String candidateName;

    @Indexed
    @Field("candidate_email")
    private String candidateEmail;

    @Field("phone")
    private String phone;

    @Field("location")
    private String location;

    @Field("linkedin_url")
    private String linkedinUrl;

    @Field("portfolio_url")
    private String portfolioUrl;

    // ── File Metadata ──────────────────────────────────────
    @Field("original_filename")
    private String originalFilename;

    @Field("stored_filename")
    private String storedFilename;

    @Field("file_path")
    private String filePath;

    @Field("file_size_bytes")
    private Long fileSizeBytes;

    @Field("content_type")
    private String contentType;

    // ── Parsed Content ─────────────────────────────────────
    @Field("raw_text")
    private String rawText;

    @Field("summary")
    private String summary;

    @Field("total_experience_years")
    private Double totalExperienceYears;

    // ── AI-Extracted Skills ────────────────────────────────
    @Field("technical_skills")
    private List<String> technicalSkills;

    @Field("soft_skills")
    private List<String> softSkills;

    @Field("certifications")
    private List<String> certifications;

    @Field("languages")
    private List<String> languages;

    // ── Education ─────────────────────────────────────────
    @Field("education")
    private List<Education> education;

    // ── Work Experience ────────────────────────────────────
    @Field("work_experience")
    private List<WorkExperience> workExperience;

    // ── Projects ───────────────────────────────────────────
    @Field("projects")
    private List<Project> projects;

    // ── Processing ────────────────────────────────────────
    @Field("processing_status")
    @Builder.Default
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

    @Field("processing_error")
    private String processingError;

    @Field("ai_confidence_score")
    private Double aiConfidenceScore;

    @Field("ai_metadata")
    private Map<String, Object> aiMetadata;

    // ── Status ────────────────────────────────────────────
    @Field("is_active")
    @Builder.Default
    private Boolean isActive = true;

    // ── Audit ─────────────────────────────────────────────
    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;

    @CreatedBy
    @Field("created_by")
    private String createdBy;

    @LastModifiedBy
    @Field("updated_by")
    private String updatedBy;

    @Version
    private Long version;

    // ── Embedded Documents ────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Education {
        private String institution;
        private String degree;
        private String fieldOfStudy;
        private String startYear;
        private String endYear;
        private Double gpa;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkExperience {
        private String company;
        private String title;
        private String location;
        private String startDate;
        private String endDate;
        private Boolean isCurrent;
        private String description;
        private List<String> technologies;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Project {
        private String name;
        private String description;
        private List<String> technologies;
        private String url;
    }

    public enum ProcessingStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
