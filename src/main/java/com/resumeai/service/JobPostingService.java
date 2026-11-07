package com.resumeai.service;

import com.resumeai.model.*;
import org.springframework.data.domain.*;

import java.util.List;

public interface JobPostingService {
    JobPosting createJob(JobPosting job, String recruiterId);
    JobPosting getJobById(String id);
    Page<JobPosting> getAllActiveJobs(Pageable pageable);
    Page<JobPosting> getJobsByRecruiter(String recruiterId, Pageable pageable);
    JobPosting updateJob(String id, JobPosting updated, String recruiterId);
    void deleteJob(String id, String recruiterId);
    List<JobPosting> findJobsBySkills(List<String> skills);
    Page<JobPosting> searchJobs(String keyword, Pageable pageable);
}
