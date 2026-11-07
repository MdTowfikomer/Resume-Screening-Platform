package com.resumeai.service.impl;

import com.resumeai.model.*;
import com.resumeai.repository.mongo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core AI-powered job matching engine.
 *
 * Scoring weights:
 *  - Skill match          : 50%
 *  - Experience match     : 25%
 *  - Education match      : 10%
 *  - Keyword/NLP match    : 15%
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private static final double WEIGHT_SKILL      = 0.50;
    private static final double WEIGHT_EXPERIENCE = 0.25;
    private static final double WEIGHT_EDUCATION  = 0.10;
    private static final double WEIGHT_KEYWORD    = 0.15;

    private final MatchResultRepository matchResultRepository;
    private final AiNlpService          aiNlpService;

    /**
     * Compute a match score between a resume and job posting.
     * Stores the result in MongoDB. Returns the populated MatchResult.
     */
    public MatchResult computeAndSaveMatch(Resume resume, JobPosting job) {
        log.info("Computing match: resume=[{}] job=[{}]", resume.getId(), job.getId());

        // Try AI score first, fallback to rule-based
        double aiScore = -1;
        try {
            aiScore = aiNlpService.computeMatchScore(
                    resume.getRawText(),
                    job.getRequiredSkills(),
                    job.getPreferredSkills());
        } catch (Exception ex) {
            log.debug("AI scoring unavailable, using rule-based: {}", ex.getMessage());
        }

        // Rule-based scoring breakdown
        SkillAnalysis skillAnalysis = analyzeSkills(
                resume.getTechnicalSkills(), job.getRequiredSkills(), job.getPreferredSkills());

        double skillScore      = computeSkillScore(skillAnalysis);
        double experienceScore = computeExperienceScore(resume, job);
        double educationScore  = computeEducationScore(resume, job);
        double keywordScore    = computeKeywordScore(resume, job);

        double overallScore;
        if (aiScore >= 0) {
            // Blend AI score (60%) with rule-based (40%)
            double ruleScore = (skillScore * WEIGHT_SKILL)
                    + (experienceScore * WEIGHT_EXPERIENCE)
                    + (educationScore  * WEIGHT_EDUCATION)
                    + (keywordScore    * WEIGHT_KEYWORD);
            overallScore = (aiScore * 0.6) + (ruleScore * 0.4);
        } else {
            overallScore = (skillScore      * WEIGHT_SKILL)
                    + (experienceScore  * WEIGHT_EXPERIENCE)
                    + (educationScore   * WEIGHT_EDUCATION)
                    + (keywordScore     * WEIGHT_KEYWORD);
        }

        overallScore = Math.min(100.0, Math.round(overallScore * 100.0) / 100.0);

        MatchResult matchResult = MatchResult.builder()
                .resumeId(resume.getId())
                .jobId(job.getId())
                .candidateId(resume.getCandidateId())
                .recruiterId(job.getRecruiterId())
                .overallScore(overallScore)
                .skillMatchScore(skillScore)
                .experienceMatchScore(experienceScore)
                .educationMatchScore(educationScore)
                .keywordMatchScore(keywordScore)
                .matchedSkills(skillAnalysis.getMatched())
                .missingSkills(skillAnalysis.getMissing())
                .bonusSkills(skillAnalysis.getBonus())
                .scoreBreakdown(Map.of(
                        "skill",      skillScore,
                        "experience", experienceScore,
                        "education",  educationScore,
                        "keyword",    keywordScore
                ))
                .recommendation(toRecommendation(overallScore))
                .aiSummary(buildAiSummary(overallScore, skillAnalysis, resume, job))
                .status(MatchResult.MatchStatus.NEW)
                .build();

        // Upsert
        return matchResultRepository.findByResumeIdAndJobId(resume.getId(), job.getId())
                .map(existing -> {
                    existing.setOverallScore(matchResult.getOverallScore());
                    existing.setSkillMatchScore(matchResult.getSkillMatchScore());
                    existing.setMatchedSkills(matchResult.getMatchedSkills());
                    existing.setMissingSkills(matchResult.getMissingSkills());
                    existing.setRecommendation(matchResult.getRecommendation());
                    existing.setAiSummary(matchResult.getAiSummary());
                    return matchResultRepository.save(existing);
                })
                .orElseGet(() -> matchResultRepository.save(matchResult));
    }

    // ── Private scoring helpers ────────────────────────────

    private SkillAnalysis analyzeSkills(List<String> candidateSkills,
                                         List<String> requiredSkills,
                                         List<String> preferredSkills) {
        Set<String> cSkills = toLower(candidateSkills);
        Set<String> rSkills = toLower(requiredSkills);
        Set<String> pSkills = toLower(preferredSkills != null ? preferredSkills : List.of());

        List<String> matched = rSkills.stream()
                .filter(cSkills::contains).map(this::toTitleCase).collect(Collectors.toList());
        List<String> missing = rSkills.stream()
                .filter(s -> !cSkills.contains(s)).map(this::toTitleCase).collect(Collectors.toList());
        List<String> bonus   = pSkills.stream()
                .filter(cSkills::contains).map(this::toTitleCase).collect(Collectors.toList());

        return new SkillAnalysis(matched, missing, bonus, rSkills.size());
    }

    private double computeSkillScore(SkillAnalysis analysis) {
        if (analysis.getTotalRequired() == 0) return 100.0;
        double baseScore = ((double) analysis.getMatched().size() / analysis.getTotalRequired()) * 100.0;
        double bonusScore = Math.min(10.0, analysis.getBonus().size() * 2.5);
        return Math.min(100.0, baseScore + bonusScore);
    }

    private double computeExperienceScore(Resume resume, JobPosting job) {
        if (job.getMinExperienceYears() == null) return 100.0;
        Double candidateYears = resume.getTotalExperienceYears();
        if (candidateYears == null) return 50.0;

        int minReq = job.getMinExperienceYears();
        int maxReq = job.getMaxExperienceYears() != null ? job.getMaxExperienceYears() : minReq + 10;

        if (candidateYears >= minReq && candidateYears <= maxReq) return 100.0;
        if (candidateYears >= minReq)                              return 85.0;
        if (candidateYears >= minReq * 0.75)                      return 60.0;
        return 30.0;
    }

    private double computeEducationScore(Resume resume, JobPosting job) {
        if (job.getRequiredEducation() == null) return 100.0;
        if (resume.getEducation() == null || resume.getEducation().isEmpty()) return 50.0;
        String reqEdu = job.getRequiredEducation().toLowerCase();
        boolean matches = resume.getEducation().stream().anyMatch(edu ->
                (edu.getDegree() != null && edu.getDegree().toLowerCase().contains(reqEdu))
                || (edu.getFieldOfStudy() != null && edu.getFieldOfStudy().toLowerCase().contains(reqEdu)));
        return matches ? 100.0 : 60.0;
    }

    private double computeKeywordScore(Resume resume, JobPosting job) {
        if (resume.getRawText() == null || job.getDescription() == null) return 50.0;
        String resumeTextLower = resume.getRawText().toLowerCase();
        long totalKeywords  = Arrays.stream(job.getDescription().split("\\s+")).count();
        long matchedKeywords = Arrays.stream(job.getDescription().split("\\s+"))
                .map(String::toLowerCase)
                .filter(w -> w.length() > 4 && resumeTextLower.contains(w))
                .count();
        return totalKeywords > 0 ? Math.min(100.0, (matchedKeywords * 100.0) / totalKeywords * 3) : 50.0;
    }

    private MatchResult.MatchRecommendation toRecommendation(double score) {
        if (score >= 85) return MatchResult.MatchRecommendation.STRONG_MATCH;
        if (score >= 70) return MatchResult.MatchRecommendation.GOOD_MATCH;
        if (score >= 55) return MatchResult.MatchRecommendation.PARTIAL_MATCH;
        if (score >= 40) return MatchResult.MatchRecommendation.WEAK_MATCH;
        return MatchResult.MatchRecommendation.NO_MATCH;
    }

    private String buildAiSummary(double score, SkillAnalysis analysis, Resume resume, JobPosting job) {
        return String.format(
                "Candidate scored %.1f%% for '%s'. Matched %d/%d required skills. " +
                "Missing: %s. Experience: %.1f years (required: %d+). Recommendation: %s.",
                score, job.getTitle(),
                analysis.getMatched().size(), analysis.getTotalRequired(),
                analysis.getMissing().isEmpty() ? "none" : String.join(", ", analysis.getMissing()),
                resume.getTotalExperienceYears() != null ? resume.getTotalExperienceYears() : 0.0,
                job.getMinExperienceYears() != null ? job.getMinExperienceYears() : 0,
                toRecommendation(score).name().replace("_", " ")
        );
    }

    private Set<String> toLower(List<String> skills) {
        return skills == null ? Set.of() :
                skills.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }

    private String toTitleCase(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @lombok.Getter
    @RequiredArgsConstructor
    private static class SkillAnalysis {
        private final List<String> matched;
        private final List<String> missing;
        private final List<String> bonus;
        private final int          totalRequired;
    }
}
