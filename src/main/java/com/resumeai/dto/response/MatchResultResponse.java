package com.resumeai.dto.response;
// ═══════════════════════════════════════════════
//  MATCH RESPONSE
// ═══════════════════════════════════════════════

import com.fasterxml.jackson.annotation.JsonInclude;
import com.resumeai.model.MatchResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class MatchResultResponse {
    private String id;
    private String resumeId;
    private String jobId;
    private String candidateId;
    private Double overallScore;
    private Double skillMatchScore;
    private Double experienceMatchScore;
    private Double educationMatchScore;
    private Double keywordMatchScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> bonusSkills;
    private Map<String, Double> scoreBreakdown;
    private String aiSummary;
    private MatchResult.MatchRecommendation recommendation;
    private MatchResult.MatchStatus status;
    private String recruiterNotes;
    private Instant createdAt;
    private Instant updatedAt;
}
