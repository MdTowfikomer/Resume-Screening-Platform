package com.resumeai.repository.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.List;

@Document(indexName = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "english")
    private String title;

    @Field(type = FieldType.Text, analyzer = "english")
    private String description;

    @Field(type = FieldType.Keyword)
    private String companyName;

    @Field(type = FieldType.Keyword)
    private List<String> requiredSkills;

    @Field(type = FieldType.Keyword)
    private List<String> preferredSkills;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Text)
    private String location;

    @Field(type = FieldType.Integer)
    private Integer minExperienceYears;

    @Field(type = FieldType.Date)
    private Instant createdAt;
}
