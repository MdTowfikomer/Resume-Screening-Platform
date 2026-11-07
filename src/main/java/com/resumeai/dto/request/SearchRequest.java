package com.resumeai.dto.request;
// ═══════════════════════════════════════════════
//  SEARCH REQUEST
// ═══════════════════════════════════════════════

import com.resumeai.model.JobPosting;
import lombok.Data;

import java.util.List;

class SearchRequest {

    @Data
    public static class ResumeSearch {
        private String keyword;
        private List<String> skills;
        private Double minExperience;
        private Double maxExperience;
        private String location;
        private String processingStatus;
        private int page = 0;
        private int size = 20;
        private String sortBy = "createdAt";
        private String sortDir = "desc";
    }

    @Data
    public static class JobSearch {
        private String keyword;
        private List<String> skills;
        private String location;
        private Boolean remoteAllowed;
        private JobPosting.EmploymentType employmentType;
        private JobPosting.ExperienceLevel experienceLevel;
        private Long salaryMin;
        private Long salaryMax;
        private int page = 0;
        private int size = 20;
    }
}
