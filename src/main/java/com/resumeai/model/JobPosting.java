package com.resumeai.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
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

/**
 * MongoDB document representing a job posting created by a recruiter.
 */
@Document(collection = "job_postings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting {

    @Id
    private String id;

    // ── Job Details ────────────────────────────────────────
    @Indexed
    @Field("title")
    private String title;

    @Field("description")
    private String description;

    @Field("company_name")
    private String companyName;

    @Field("company_id")
    private String companyId;

    @Indexed
    @Field("recruiter_id")
    private String recruiterId;

    @Field("location")
    private String location;

    @Field("remote_allowed")
    private Boolean remoteAllowed;

    @Field("employment_type")
    private EmploymentType employmentType;

    @Field("experience_level")
    private ExperienceLevel experienceLevel;

    @Field("min_experience_years")
    private Integer minExperienceYears;

    @Field("max_experience_years")
    private Integer maxExperienceYears;

    // ── Compensation ───────────────────────────────────────
    @Field("salary_min")
    private Long salaryMin;

    @Field("salary_max")
    private Long salaryMax;

    @Field("salary_currency")
    @Builder.Default
    private String salaryCurrency = "USD";

    // ── Skills & Requirements ──────────────────────────────
    @Field("required_skills")
    private List<String> requiredSkills;

    @Field("preferred_skills")
    private List<String> preferredSkills;

    @Field("required_education")
    private String requiredEducation;

    @Field("responsibilities")
    private List<String> responsibilities;

    @Field("benefits")
    private List<String> benefits;

    // ── Status & Metadata ──────────────────────────────────
    @Field("status")
    @Builder.Default
    private JobStatus status = JobStatus.ACTIVE;

    @Indexed
    @Field("expires_at")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant expiresAt;

    @Field("application_count")
    @Builder.Default
    private Integer applicationCount = 0;

    @Field("view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Field("tags")
    private List<String> tags;

    // ── Audit ─────────────────────────────────────────────
    @CreatedDate
    @Field("created_at")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant updatedAt;

    @CreatedBy
    @Field("created_by")
    private String createdBy;

    @LastModifiedBy
    @Field("updated_by")
    private String updatedBy;

    @Version
    private Long version;

    public enum JobStatus {DRAFT, ACTIVE, PAUSED, CLOSED, EXPIRED}

    public enum EmploymentType {FULL_TIME, PART_TIME, CONTRACT, FREELANCE, INTERNSHIP}

    public enum ExperienceLevel {ENTRY, JUNIOR, MID, SENIOR, LEAD, PRINCIPAL, EXECUTIVE}
}
