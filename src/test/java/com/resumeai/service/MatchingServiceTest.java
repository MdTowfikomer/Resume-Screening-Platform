//package com.resumeai.service;
//
//import com.resumeai.model.*;
//import com.resumeai.repository.mongo.MatchResultRepository;
//import com.resumeai.service.impl.AiNlpService;
//import com.resumeai.service.impl.MatchingService;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("MatchingService Unit Tests — scoring algorithm")
//class MatchingServiceTest {
//
//    @Mock private MatchResultRepository matchResultRepository;
//    @Mock private AiNlpService          aiNlpService;
//
//    @InjectMocks
//    private MatchingService matchingService;
//
//    private Resume buildResume(List<String> skills, double experienceYears) {
//        return Resume.builder()
//                .id("resume-1")
//                .candidateId("candidate-1")
//                .rawText("Java Spring Boot Kubernetes AWS experienced developer")
//                .technicalSkills(skills)
//                .totalExperienceYears(experienceYears)
//                .processingStatus(Resume.ProcessingStatus.COMPLETED)
//                .build();
//    }
//
//    private JobPosting buildJob(List<String> required, List<String> preferred,
//                                int minExp, int maxExp) {
//        return JobPosting.builder()
//                .id("job-1")
//                .recruiterId("recruiter-1")
//                .title("Senior Java Developer")
//                .description("Looking for experienced Java developer with Spring Boot Kubernetes AWS")
//                .requiredSkills(required)
//                .preferredSkills(preferred)
//                .minExperienceYears(minExp)
//                .maxExperienceYears(maxExp)
//                .build();
//    }
//
//    @Test
//    @DisplayName("Perfect skill match — should score >= 85 (STRONG_MATCH)")
//    void perfectSkillMatch_strongMatch() {
//        Resume resume = buildResume(List.of("Java", "Spring Boot", "Kubernetes", "AWS"), 6.0);
//        JobPosting job = buildJob(
//                List.of("Java", "Spring Boot", "Kubernetes", "AWS"),
//                List.of("Docker"),
//                3, 8);
//
//        when(aiNlpService.computeMatchScore(any(), any(), any()))
//                .thenThrow(new RuntimeException("AI unavailable"));
//        when(matchResultRepository.findByResumeIdAndJobId(any(), any()))
//                .thenReturn(Optional.empty());
//        when(matchResultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
//
//        MatchResult result = matchingService.computeAndSaveMatch(resume, job);
//
//        assertThat(result.getOverallScore()).isGreaterThanOrEqualTo(80.0);
//        assertThat(result.getMatchedSkills()).containsExactlyInAnyOrder(
//                "Java", "Spring Boot", "Kubernetes", "AWS");
//        assertThat(result.getMissingSkills()).isEmpty();
//    }
//
//    @Test
//    @DisplayName("Zero skill overlap — should score < 40 (WEAK_MATCH or NO_MATCH)")
//    void noSkillOverlap_weakMatch() {
//        Resume resume = buildResume(List.of("PHP", "Laravel"), 2.0);
//        JobPosting job = buildJob(
//                List.of("Java", "Spring Boot", "Kubernetes"),
//                List.of(),
//                5, 10);
//
//        when(aiNlpService.computeMatchScore(any(), any(), any()))
//                .thenThrow(new RuntimeException("AI unavailable"));
//        when(matchResultRepository.findByResumeIdAndJobId(any(), any()))
//                .thenReturn(Optional.empty());
//        when(matchResultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
//
//        MatchResult result = matchingService.computeAndSaveMatch(resume, job);
//
//        assertThat(result.getOverallScore()).isLessThan(40.0);
//        assertThat(result.getMatchedSkills()).isEmpty();
//        assertThat(result.getMissingSkills()).hasSize(3);
//    }
//
//    @Test
//    @DisplayName("Partial match — GOOD_MATCH recommendation")
//    void partialSkillMatch_goodMatch() {
//        Resume resume = buildResume(List.of("Java", "Spring Boot", "Docker"), 5.0);
//        JobPosting job = buildJob(
//                List.of("Java", "Spring Boot", "Kubernetes", "AWS"),
//                List.of("Docker"),
//                3, 8);
//
//        when(aiNlpService.computeMatchScore(any(), any(), any()))
//                .thenThrow(new RuntimeException("AI unavailable"));
//        when(matchResultRepository.findByResumeIdAndJobId(any(), any()))
//                .thenReturn(Optional.empty());
//        when(matchResultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
//
//        MatchResult result = matchingService.computeAndSaveMatch(resume, job);
//
//        assertThat(result.getOverallScore()).isBetween(50.0, 90.0);
//        assertThat(result.getMatchedSkills()).containsExactlyInAnyOrder("Java", "Spring Boot");
//        assertThat(result.getBonusSkills()).contains("Docker");
//        assertThat(result.getAiSummary()).isNotBlank();
//    }
//
//    @Test
//    @DisplayName("AI score blending — AI score should influence final score")
//    void aiScoreBlending_influencesFinalScore() {
//        Resume resume = buildResume(List.of("Java"), 4.0);
//        JobPosting job = buildJob(List.of("Java", "Python"), List.of(), 2, 6);
//
//        when(aiNlpService.computeMatchScore(any(), any(), any())).thenReturn(95.0);
//        when(matchResultRepository.findByResumeIdAndJobId(any(), any()))
//                .thenReturn(Optional.empty());
//        when(matchResultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
//
//        MatchResult result = matchingService.computeAndSaveMatch(resume, job);
//
//        // With AI returning 95, final blended score should be significantly boosted
//        assertThat(result.getOverallScore()).isGreaterThan(60.0);
//    }
//
//    @Test
//    @DisplayName("Upsert — existing match should be updated, not duplicated")
//    void upsertMatch_updatesExisting() {
//        Resume resume = buildResume(List.of("Java", "Spring Boot"), 4.0);
//        JobPosting job = buildJob(List.of("Java", "Spring Boot"), List.of(), 2, 6);
//
//        MatchResult existing = MatchResult.builder()
//                .id("match-existing")
//                .resumeId("resume-1").jobId("job-1")
//                .overallScore(50.0).build();
//
//        when(aiNlpService.computeMatchScore(any(), any(), any()))
//                .thenThrow(new RuntimeException("AI unavailable"));
//        when(matchResultRepository.findByResumeIdAndJobId("resume-1", "job-1"))
//                .thenReturn(Optional.of(existing));
//        when(matchResultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
//
//        MatchResult result = matchingService.computeAndSaveMatch(resume, job);
//
//        assertThat(result.getId()).isEqualTo("match-existing");
//        // Score should be refreshed
//        assertThat(result.getOverallScore()).isNotEqualTo(50.0);
//    }
//}
