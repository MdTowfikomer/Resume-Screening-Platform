package com.resumeai.service;

import com.resumeai.model.MatchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MatchService {
    MatchResult triggerMatch(String resumeId, String jobId);
    void triggerBulkMatch(String jobId, double scoreThreshold);
    MatchResult getMatchById(String id);
    Page<MatchResult> getMatchesByJob(String jobId, Pageable pageable);
    Page<MatchResult> getMatchesByResume(String resumeId, Pageable pageable);
    List<MatchResult> getTopMatchesForJob(String jobId, double threshold);
    MatchResult updateMatchStatus(String id, MatchResult.MatchStatus status, String notes, String recruiterId);
    void deleteMatch(String id);
}
