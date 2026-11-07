package com.resumeai.repository.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface JobSearchRepository extends ElasticsearchRepository<JobDocument, String> {

    List<JobDocument> findByRequiredSkillsIn(List<String> skills);

    List<JobDocument> findByStatusAndMinExperienceYearsLessThanEqual(
            String status, Integer experienceYears);
}
