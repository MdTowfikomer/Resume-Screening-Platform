package com.resumeai.service.impl;

import com.resumeai.exception.ResourceNotFoundException;
import com.resumeai.model.JobPosting;
import com.resumeai.repository.mongo.JobPostingRepository;
import com.resumeai.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobPostingServiceImpl implements JobPostingService {

    private final JobPostingRepository jobPostingRepository;

    @Override
    public JobPosting createJob(JobPosting job, String recruiterId) {
        job.setRecruiterId(recruiterId);
        job.setStatus(JobPosting.JobStatus.ACTIVE);
        JobPosting saved = jobPostingRepository.save(job);
        log.info("Job created [{}] by recruiter [{}]", saved.getId(), recruiterId);
        return saved;
    }

    @Override
    @Cacheable(value = "jobs", key = "#id")
    public JobPosting getJobById(String id) {
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobPosting", "id", id));
    }

    @Override
    public Page<JobPosting> getAllActiveJobs(Pageable pageable) {
        return jobPostingRepository.findByStatus(JobPosting.JobStatus.ACTIVE, pageable);
    }

    @Override
    public Page<JobPosting> getJobsByRecruiter(String recruiterId, Pageable pageable) {
        return jobPostingRepository.findByRecruiterId(recruiterId, pageable);
    }

    @Override
    @CacheEvict(value = "jobs", key = "#id")
    public JobPosting updateJob(String id, JobPosting updated, String recruiterId) {
        JobPosting existing = getJobById(id);
        if (!existing.getRecruiterId().equals(recruiterId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not own this job posting");
        }
        if (updated.getTitle()              != null) existing.setTitle(updated.getTitle());
        if (updated.getDescription()        != null) existing.setDescription(updated.getDescription());
        if (updated.getRequiredSkills()     != null) existing.setRequiredSkills(updated.getRequiredSkills());
        if (updated.getPreferredSkills()    != null) existing.setPreferredSkills(updated.getPreferredSkills());
        if (updated.getStatus()             != null) existing.setStatus(updated.getStatus());
        if (updated.getSalaryMin()          != null) existing.setSalaryMin(updated.getSalaryMin());
        if (updated.getSalaryMax()          != null) existing.setSalaryMax(updated.getSalaryMax());
        if (updated.getExpiresAt()          != null) existing.setExpiresAt(updated.getExpiresAt());
        return jobPostingRepository.save(existing);
    }

    @Override
    @CacheEvict(value = "jobs", key = "#id")
    public void deleteJob(String id, String recruiterId) {
        JobPosting job = getJobById(id);
        if (!job.getRecruiterId().equals(recruiterId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not own this job posting");
        }
        job.setStatus(JobPosting.JobStatus.CLOSED);
        jobPostingRepository.save(job);
    }

    @Override
    public List<JobPosting> findJobsBySkills(List<String> skills) {
        return jobPostingRepository.findActiveJobsByRequiredSkills(skills);
    }

    @Override
    public Page<JobPosting> searchJobs(String keyword, Pageable pageable) {
        return jobPostingRepository.findByTitleContainingIgnoreCaseAndStatusActive(keyword, pageable);
    }
}
