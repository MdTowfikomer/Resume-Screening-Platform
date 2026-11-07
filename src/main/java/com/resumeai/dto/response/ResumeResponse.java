package com.resumeai.dto.response;
// ═══════════════════════════════════════════════
//  RESUME RESPONSE
// ═══════════════════════════════════════════════

import com.fasterxml.jackson.annotation.JsonInclude;
import com.resumeai.model.Resume;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class ResumeResponse {
    private String id;
    private String candidateId;
    private String candidateName;
    private String candidateEmail;
    private String phone;
    private String location;
    private String originalFilename;
    private Long fileSizeBytes;
    private String summary;
    private Double totalExperienceYears;
    private List<String> technicalSkills;
    private List<String> softSkills;
    private List<String> certifications;
    private List<Resume.Education> education;
    private List<Resume.WorkExperience> workExperience;
    private Resume.ProcessingStatus processingStatus;
    private Double aiConfidenceScore;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
