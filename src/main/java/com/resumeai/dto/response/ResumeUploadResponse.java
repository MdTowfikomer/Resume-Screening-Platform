package com.resumeai.dto.response;
// ═══════════════════════════════════════════════
//  UPLOAD RESPONSE
// ═══════════════════════════════════════════════

import com.resumeai.model.Resume;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ResumeUploadResponse {
    private String resumeId;
    private String originalFilename;
    private Long fileSizeBytes;
    private Resume.ProcessingStatus processingStatus;
    private String message;
    private Instant submittedAt;
}
