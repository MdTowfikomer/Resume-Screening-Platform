package com.resumeai.dto.response;
// ═══════════════════════════════════════════════
//  JOB RESPONSE
// ═══════════════════════════════════════════════

import com.fasterxml.jackson.annotation.JsonInclude;
import com.resumeai.model.JobPosting;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
class JobPostingResponse {
    private String id;
    private String title;
    private String description;
    private String companyName;
    private String location;
    private Boolean remoteAllowed;
    private JobPosting.EmploymentType employmentType;
    private JobPosting.ExperienceLevel experienceLevel;
    private Integer minExperienceYears;
    private Integer maxExperienceYears;
    private Long salaryMin;
    private Long salaryMax;
    private String salaryCurrency;
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private JobPosting.JobStatus status;
    private Integer applicationCount;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
}
