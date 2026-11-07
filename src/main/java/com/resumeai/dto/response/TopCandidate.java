package com.resumeai.dto.response;

import com.resumeai.model.MatchResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TopCandidate {
    private String resumeId;
    private String candidateName;
    private String candidateEmail;
    private String jobTitle;
    private Double matchScore;
    private MatchResult.MatchRecommendation recommendation;
}
