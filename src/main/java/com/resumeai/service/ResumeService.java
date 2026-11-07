package com.resumeai.service;

import com.resumeai.model.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResumeService {
    Resume uploadResume(MultipartFile file, String candidateId, String candidateEmail, String candidateName);
    Resume getResumeById(String id);
    Resume getResumeByCandidateEmail(String email);
    Page<Resume> getAllResumes(Pageable pageable);
    Page<Resume> getResumesByStatus(Resume.ProcessingStatus status, Pageable pageable);
    List<Resume> searchBySkills(List<String> skills);
    Resume updateResume(String id, Resume resume);
    void deleteResume(String id);
    Resume reprocessResume(String id);
    long getResumeCount();
}
