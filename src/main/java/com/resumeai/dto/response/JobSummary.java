package com.resumeai.dto.response;

import com.resumeai.model.JobPosting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class JobSummary {
    private String id;
    private String title;
    private Long applicationCount;
    private JobPosting.JobStatus status;
    private Instant createdAt;
}
