package com.resumeai.service.impl;

import com.resumeai.model.*;
import com.resumeai.repository.mongo.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecruiterDashboardService {

    private final JobPostingRepository  jobPostingRepository;
    private final MatchResultRepository matchResultRepository;
    private final ResumeRepository      resumeRepository;

    @Cacheable(value = "recruiter-stats", key = "#recruiterId")
    public DashboardStats getDashboardStats(String recruiterId) {
        long totalJobs        = jobPostingRepository.countByRecruiterId(recruiterId);
        long activeJobs       = jobPostingRepository.countByRecruiterIdAndStatus(
                recruiterId, JobPosting.JobStatus.ACTIVE);
        long totalApplications = matchResultRepository.countByRecruiterId(recruiterId);
        long shortlisted      = matchResultRepository.countByRecruiterIdAndStatus(
                recruiterId, MatchResult.MatchStatus.SHORTLISTED);
        long hired            = matchResultRepository.countByRecruiterIdAndStatus(
                recruiterId, MatchResult.MatchStatus.HIRED);

        // Recent job postings
        var recentJobsPage = jobPostingRepository.findByRecruiterId(
                recruiterId, PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<JobSummary> recentJobs = recentJobsPage.getContent().stream()
                .map(j -> new JobSummary(
                        j.getId(), j.getTitle(),
                        matchResultRepository.countByJobId(j.getId()),
                        j.getStatus(), j.getCreatedAt()))
                .collect(Collectors.toList());

        // Top candidates across all recruiter's jobs
        var recruiterJobs = jobPostingRepository.findByRecruiterId(
                recruiterId, PageRequest.of(0, 100)).getContent();

        List<TopCandidate> topCandidates = recruiterJobs.stream()
                .flatMap(job -> matchResultRepository
                        .findByJobIdAndOverallScoreGreaterThanEqual(job.getId(), 70.0)
                        .stream()
                        .map(m -> new TopCandidate(m, job.getTitle())))
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .limit(10)
                .collect(Collectors.toList());

        return new DashboardStats(totalJobs, activeJobs, totalApplications,
                shortlisted, hired, recentJobs, topCandidates);
    }

    // ── Inner record types ────────────────────────────────────

    public record DashboardStats(
            long totalJobs,
            long activeJobs,
            long totalApplications,
            long shortlisted,
            long hired,
            List<JobSummary> recentJobs,
            List<TopCandidate> topCandidates
    ) {}

    public record JobSummary(
            String id,
            String title,
            long applicationCount,
            JobPosting.JobStatus status,
            java.time.Instant createdAt
    ) {}

    public record TopCandidate(
            String resumeId,
            String candidateId,
            Double score,
            MatchResult.MatchRecommendation recommendation,
            MatchResult.MatchStatus status,
            String jobTitle
    ) {
        public TopCandidate(MatchResult m, String jobTitle) {
            this(m.getResumeId(), m.getCandidateId(), m.getOverallScore(),
                 m.getRecommendation(), m.getStatus(), jobTitle);
        }
    }
}
