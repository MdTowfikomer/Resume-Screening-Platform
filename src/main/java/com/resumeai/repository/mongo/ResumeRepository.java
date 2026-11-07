package com.resumeai.repository.mongo;

import com.resumeai.model.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Resume documents.
 * Extends MongoRepository for built-in CRUD + custom query methods.
 */
@Repository
public interface ResumeRepository extends MongoRepository<Resume, String> {

    Optional<Resume> findByCandidateEmailAndIsActiveTrue(String email);

    List<Resume> findByCandidateIdAndIsActiveTrue(String candidateId);

    Page<Resume> findByIsActiveTrue(Pageable pageable);

    Page<Resume> findByProcessingStatus(Resume.ProcessingStatus status, Pageable pageable);

    List<Resume> findByProcessingStatus(Resume.ProcessingStatus status);

    boolean existsByCandidateEmailAndIsActiveTrue(String email);

    long countByProcessingStatus(Resume.ProcessingStatus status);

    @Query("{ 'technical_skills': { $in: ?0 }, 'is_active': true }")
    List<Resume> findByTechnicalSkillsIn(List<String> skills);

    @Query("{ 'technical_skills': { $regex: ?0, $options: 'i' }, 'is_active': true }")
    List<Resume> findBySkillContainingIgnoreCase(String skill);

    @Query(value = "{ 'is_active': true }", count = true)
    long countActiveResumes();

    @Query("{ 'total_experience_years': { $gte: ?0, $lte: ?1 }, 'is_active': true }")
    Page<Resume> findByExperienceRange(double minYears, double maxYears, Pageable pageable);
}
