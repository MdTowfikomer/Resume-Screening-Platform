package com.resumeai.service.impl;

import com.resumeai.exception.ResourceNotFoundException;
import com.resumeai.kafka.producer.ResumeEventProducer;
import com.resumeai.model.*;
import com.resumeai.repository.mongo.*;
import com.resumeai.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchServiceImpl implements MatchService {

    private final ResumeRepository      resumeRepository;
    private final JobPostingRepository  jobPostingRepository;
    private final MatchResultRepository matchResultRepository;
    private final MatchingService       matchingService;
    private final ResumeEventProducer   eventProducer;

    @Override
    public MatchResult triggerMatch(String resumeId, String jobId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("JobPosting", "id", jobId));

        if (resume.getProcessingStatus() != Resume.ProcessingStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Resume [" + resumeId + "] is not yet processed. Status: " + resume.getProcessingStatus());
        }

        MatchResult result = matchingService.computeAndSaveMatch(resume, job);
        log.info("Match computed: resume=[{}] job=[{}] score=[{}]",
                resumeId, jobId, result.getOverallScore());

        // Publish job-matched event for notifications
        eventProducer.publishJobMatched(
                ResumeEventProducer.buildJobMatchedEvent(result, resume, job));

        return result;
    }

    @Override
    @Async("matchingExecutor")
    public void triggerBulkMatch(String jobId, double scoreThreshold) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("JobPosting", "id", jobId));

        List<Resume> resumes = resumeRepository
                .findByProcessingStatus(Resume.ProcessingStatus.COMPLETED);

        log.info("Bulk matching [{}] resumes against job [{}]", resumes.size(), jobId);

        int matched = 0;
        for (Resume resume : resumes) {
            try {
                MatchResult result = matchingService.computeAndSaveMatch(resume, job);
                if (result.getOverallScore() >= scoreThreshold) matched++;
            } catch (Exception ex) {
                log.warn("Failed to match resume [{}] to job [{}]: {}",
                        resume.getId(), jobId, ex.getMessage());
            }
        }
        log.info("Bulk match complete: {} candidates scored above threshold {}", matched, scoreThreshold);
    }

    @Override
    @Cacheable(value = "match-scores", key = "#id")
    public MatchResult getMatchById(String id) {
        return matchResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MatchResult", "id", id));
    }

    @Override
    public Page<MatchResult> getMatchesByJob(String jobId, Pageable pageable) {
        return matchResultRepository.findByJobIdOrderByOverallScoreDesc(jobId, pageable);
    }

    @Override
    public Page<MatchResult> getMatchesByResume(String resumeId, Pageable pageable) {
        return matchResultRepository.findByResumeId(resumeId, pageable);
    }

    @Override
    public List<MatchResult> getTopMatchesForJob(String jobId, double threshold) {
        return matchResultRepository.findByJobIdAndOverallScoreGreaterThanEqual(jobId, threshold);
    }

    @Override
    @CacheEvict(value = "match-scores", key = "#id")
    public MatchResult updateMatchStatus(String id, MatchResult.MatchStatus status,
                                          String notes, String recruiterId) {
        MatchResult match = getMatchById(id);
        match.setStatus(status);
        if (notes != null) match.setRecruiterNotes(notes);
        match.setReviewedAt(Instant.now());
        match.setReviewedBy(recruiterId);
        return matchResultRepository.save(match);
    }

    @Override
    @CacheEvict(value = "match-scores", key = "#id")
    public void deleteMatch(String id) {
        if (!matchResultRepository.existsById(id)) {
            throw new ResourceNotFoundException("MatchResult", "id", id);
        }
        matchResultRepository.deleteById(id);
    }
}
