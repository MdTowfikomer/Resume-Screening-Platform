package com.resumeai.repository.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.Instant;
import java.util.List;

/**
 * Elasticsearch document for full-text resume search and matching.
 * Mirrors key fields from the MongoDB Resume document.
 */
@Document(indexName = "resumes")
@Setting(settingPath = "/elasticsearch/resume-settings.json")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String candidateId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String candidateName;

    @Field(type = FieldType.Keyword)
    private String candidateEmail;

    @Field(type = FieldType.Text, analyzer = "english")
    private String rawText;

    @Field(type = FieldType.Text, analyzer = "english")
    private String summary;

    @Field(type = FieldType.Keyword)
    private List<String> technicalSkills;

    @Field(type = FieldType.Keyword)
    private List<String> softSkills;

    @Field(type = FieldType.Keyword)
    private List<String> certifications;

    @Field(type = FieldType.Double)
    private Double totalExperienceYears;

    @Field(type = FieldType.Text)
    private String location;

    @Field(type = FieldType.Keyword)
    private String processingStatus;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    @Field(type = FieldType.Date)
    private Instant updatedAt;
}
