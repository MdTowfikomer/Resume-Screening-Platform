package com.resumeai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Stores the AI-computed match result between a Resume and a JobPosting.
 */
@Document(collection = "match_results")
@CompoundIndexes({
        @CompoundIndex(name = "resume_job_idx", def = "{'resume_id': 1, 'job_id': 1}", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResult {

    @Id
    private String id;

    @Indexed
    @Field("resume_id")
    private String resumeId;

    @Indexed
    @Field("job_id")
    private String jobId;

    @Field("candidate_id")
    private String candidateId;

    @Field("recruiter_id")
    private String recruiterId;

    // ── Scoring ────────────────────────────────────────────
    @Field("overall_score")
    private Double overallScore;

    @Field("skill_match_score")
    private Double skillMatchScore;

    @Field("experience_match_score")
    private Double experienceMatchScore;

    @Field("education_match_score")
    private Double educationMatchScore;

    @Field("keyword_match_score")
    private Double keywordMatchScore;

    // ── Detailed Analysis ──────────────────────────────────
    @Field("matched_skills")
    private List<String> matchedSkills;

    @Field("missing_skills")
    private List<String> missingSkills;

    @Field("bonus_skills")
    private List<String> bonusSkills;

    @Field("score_breakdown")
    private Map<String, Double> scoreBreakdown;

    @Field("ai_summary")
    private String aiSummary;

    @Field("recommendation")
    private MatchRecommendation recommendation;

    // ── Status ─────────────────────────────────────────────
    @Field("status")
    @Builder.Default
    private MatchStatus status = MatchStatus.NEW;

    @Field("recruiter_notes")
    private String recruiterNotes;

    @Field("reviewed_at")
    private Instant reviewedAt;

    @Field("reviewed_by")
    private String reviewedBy;

    // ── Audit ──────────────────────────────────────────────
    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;

    public enum MatchStatus {NEW, REVIEWED, SHORTLISTED, REJECTED, HIRED}

    public enum MatchRecommendation {STRONG_MATCH, GOOD_MATCH, PARTIAL_MATCH, WEAK_MATCH, NO_MATCH}
}
