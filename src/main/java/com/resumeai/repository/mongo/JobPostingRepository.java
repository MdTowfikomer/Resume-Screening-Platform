package com.resumeai.repository.mongo;

import com.resumeai.model.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface JobPostingRepository extends MongoRepository<JobPosting, String> {

    Page<JobPosting> findByStatus(JobPosting.JobStatus status, Pageable pageable);

    Page<JobPosting> findByRecruiterId(String recruiterId, Pageable pageable);

    Page<JobPosting> findByRecruiterIdAndStatus(
            String recruiterId, JobPosting.JobStatus status, Pageable pageable);

    List<JobPosting> findByStatusAndExpiresAtBefore(JobPosting.JobStatus status, Instant now);

    @Query("{ 'required_skills': { $in: ?0 }, 'status': 'ACTIVE' }")
    List<JobPosting> findActiveJobsByRequiredSkills(List<String> skills);

    @Query("{ 'title': { $regex: ?0, $options: 'i' }, 'status': 'ACTIVE' }")
    Page<JobPosting> findByTitleContainingIgnoreCaseAndStatusActive(String title, Pageable pageable);

    long countByRecruiterId(String recruiterId);

    long countByStatus(JobPosting.JobStatus status);

    long countByRecruiterIdAndStatus(String recruiterId, JobPosting.JobStatus status);
}
