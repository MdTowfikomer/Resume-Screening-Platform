package com.resumeai.repository.elasticsearch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeSearchRepository
        extends ElasticsearchRepository<ResumeDocument, String> {

    List<ResumeDocument> findByTechnicalSkillsIn(List<String> skills);

    Page<ResumeDocument> findByCandidateNameContaining(String name, Pageable pageable);

    List<ResumeDocument> findByTotalExperienceYearsGreaterThanEqual(Double years);
}
