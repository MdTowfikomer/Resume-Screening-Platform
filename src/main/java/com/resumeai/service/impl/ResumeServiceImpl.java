package com.resumeai.service.impl;

import com.resumeai.exception.DuplicateResourceException;
import com.resumeai.exception.ResourceNotFoundException;
import com.resumeai.kafka.producer.ResumeEventProducer;
import com.resumeai.model.Resume;
import com.resumeai.repository.elasticsearch.ResumeDocument;
import com.resumeai.repository.elasticsearch.ResumeSearchRepository;
import com.resumeai.repository.mongo.ResumeRepository;
import com.resumeai.service.ResumeService;
import com.resumeai.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository      resumeRepository;
    private final ResumeSearchRepository resumeSearchRepository;
    private final FileStorageUtil        fileStorageUtil;
    private final ResumeEventProducer    eventProducer;

    @Override
    public Resume uploadResume(MultipartFile file, String candidateId,
                               String candidateEmail, String candidateName) {
        if (resumeRepository.existsByCandidateEmailAndIsActiveTrue(candidateEmail)) {
            throw new DuplicateResourceException(
                    "A resume already exists for: " + candidateEmail + ". Delete or update it.");
        }

        String storedFilename = null;
        try {
            storedFilename = fileStorageUtil.storeFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
            // TODO
        }

        Resume resume = Resume.builder()
                .candidateId(candidateId)
                .candidateEmail(candidateEmail)
                .candidateName(candidateName)
                .originalFilename(file.getOriginalFilename())
                .storedFilename(storedFilename)
                .filePath(storedFilename)
                .fileSizeBytes(file.getSize())
                .contentType(file.getContentType())
                .processingStatus(Resume.ProcessingStatus.PENDING)
                .isActive(true)
                .build();

        resume = resumeRepository.save(resume);
        log.info("Resume saved [{}] for candidate [{}]", resume.getId(), candidateEmail);

        // Publish async processing event
        final String resumeId       = resume.getId();
        final String finalEmail     = candidateEmail;
        final String finalFilename  = storedFilename;
        final String finalType      = file.getContentType();

        eventProducer.publishResumeUploaded(
            com.resumeai.kafka.producer.ResumeEventProducer.buildUploadedEvent(
                resumeId, candidateId, finalEmail, finalFilename, finalType));

        return resume;
    }

    @Override
    @Cacheable(value = "resumes", key = "#id")
    public Resume getResumeById(String id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", id));
    }

    @Override
    public Resume getResumeByCandidateEmail(String email) {
        return resumeRepository.findByCandidateEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "email", email));
    }

    @Override
    public Page<Resume> getAllResumes(Pageable pageable) {
        return resumeRepository.findByIsActiveTrue(pageable);
    }

    @Override
    public Page<Resume> getResumesByStatus(Resume.ProcessingStatus status, Pageable pageable) {
        return resumeRepository.findByProcessingStatus(status, pageable);
    }

    @Override
    public List<Resume> searchBySkills(List<String> skills) {
        return resumeRepository.findByTechnicalSkillsIn(skills);
    }

    @Override
    @CacheEvict(value = "resumes", key = "#id")
    public Resume updateResume(String id, Resume updated) {
        Resume existing = getResumeById(id);
        if (updated.getCandidateName()  != null) existing.setCandidateName(updated.getCandidateName());
        if (updated.getPhone()          != null) existing.setPhone(updated.getPhone());
        if (updated.getLocation()       != null) existing.setLocation(updated.getLocation());
        if (updated.getLinkedinUrl()    != null) existing.setLinkedinUrl(updated.getLinkedinUrl());
        return resumeRepository.save(existing);
    }

    @Override
    @CacheEvict(value = "resumes", key = "#id")
    public void deleteResume(String id) {
        Resume resume = getResumeById(id);
        resume.setIsActive(false);
        resumeRepository.save(resume);
        resumeSearchRepository.deleteById(id);
        log.info("Resume [{}] soft-deleted", id);
    }

    @Override
    @CacheEvict(value = "resumes", key = "#id")
    public Resume reprocessResume(String id) {
        Resume resume = getResumeById(id);
        resume.setProcessingStatus(Resume.ProcessingStatus.PENDING);
        resume.setProcessingError(null);
        resume = resumeRepository.save(resume);

        eventProducer.publishResumeUploaded(
            com.resumeai.kafka.producer.ResumeEventProducer.buildUploadedEvent(
                resume.getId(), resume.getCandidateId(), resume.getCandidateEmail(),
                resume.getStoredFilename(), resume.getContentType()));

        return resume;
    }

    @Override
    public long getResumeCount() {
        return resumeRepository.countActiveResumes();
    }

    @Async("resumeProcessingExecutor")
    public void indexToElasticsearch(Resume resume) {
        try {
            ResumeDocument doc = ResumeDocument.builder()
                    .id(resume.getId())
                    .candidateId(resume.getCandidateId())
                    .candidateName(resume.getCandidateName())
                    .candidateEmail(resume.getCandidateEmail())
                    .rawText(resume.getRawText())
                    .summary(resume.getSummary())
                    .technicalSkills(resume.getTechnicalSkills())
                    .softSkills(resume.getSoftSkills())
                    .certifications(resume.getCertifications())
                    .totalExperienceYears(resume.getTotalExperienceYears())
                    .location(resume.getLocation())
                    .processingStatus(resume.getProcessingStatus().name())
                    .createdAt(resume.getCreatedAt())
                    .updatedAt(resume.getUpdatedAt())
                    .build();
            resumeSearchRepository.save(doc);
            log.debug("Resume [{}] indexed to Elasticsearch", resume.getId());
        } catch (Exception ex) {
            log.warn("Failed to index resume [{}] to ES: {}", resume.getId(), ex.getMessage());
        }
    }
}
