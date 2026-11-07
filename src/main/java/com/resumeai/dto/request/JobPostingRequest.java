package com.resumeai.dto.request;
// ═══════════════════════════════════════════════
//  JOB POSTING REQUESTS
// ═══════════════════════════════════════════════

import com.resumeai.model.JobPosting;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.List;

class JobPostingRequest {

    @Data
    public static class Create {
        @NotBlank
        @Size(max = 200)
        private String title;

        @NotBlank
        private String description;

        @NotBlank
        private String companyName;

        private String location;
        private Boolean remoteAllowed;

        @NotNull
        private JobPosting.EmploymentType employmentType;

        @NotNull
        private JobPosting.ExperienceLevel experienceLevel;

        @Min(0)
        @Max(40)
        private Integer minExperienceYears;

        @Min(0)
        @Max(40)
        private Integer maxExperienceYears;

        private Long salaryMin;
        private Long salaryMax;
        private String salaryCurrency;

        @NotEmpty
        private List<String> requiredSkills;

        private List<String> preferredSkills;
        private List<String> responsibilities;
        private List<String> benefits;
        private List<String> tags;
        private String requiredEducation;
        private Instant expiresAt;
    }

    @Data
    public static class Update extends Create {
        private JobPosting.JobStatus status;
    }
}
