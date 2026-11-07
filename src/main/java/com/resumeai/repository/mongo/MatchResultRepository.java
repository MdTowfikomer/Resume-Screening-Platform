package com.resumeai.repository.mongo;

import com.resumeai.model.MatchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchResultRepository extends MongoRepository<MatchResult, String> {

    Optional<MatchResult> findByResumeIdAndJobId(String resumeId, String jobId);

    Page<MatchResult> findByJobId(String jobId, Pageable pageable);

    Page<MatchResult> findByResumeId(String resumeId, Pageable pageable);

    Page<MatchResult> findByRecruiterId(String recruiterId, Pageable pageable);

    List<MatchResult> findByJobIdAndOverallScoreGreaterThanEqual(String jobId, double threshold);

    Page<MatchResult> findByJobIdOrderByOverallScoreDesc(String jobId, Pageable pageable);

    @Query("{ 'job_id': ?0, 'status': { $in: ?1 } }")
    Page<MatchResult> findByJobIdAndStatusIn(
            String jobId, List<MatchResult.MatchStatus> statuses, Pageable pageable);

    long countByJobId(String jobId);

    long countByJobIdAndStatus(String jobId, MatchResult.MatchStatus status);

    long countByRecruiterId(String recruiterId);

    long countByRecruiterIdAndStatus(String recruiterId, MatchResult.MatchStatus status);

    boolean existsByResumeIdAndJobId(String resumeId, String jobId);
}
