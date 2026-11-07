package com.resumeai.dto.response;
// ═══════════════════════════════════════════════
//  DASHBOARD / STATS RESPONSE
// ═══════════════════════════════════════════════

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class RecruiterDashboardResponse {
    private Long totalJobPostings;
    private Long activeJobPostings;
    private Long totalApplications;
    private Long shortlistedCandidates;
    private Long hiredCandidates;
    private Double avgMatchScore;
    private List<JobSummary> recentJobs;
    private List<TopCandidate> topCandidates;
}
