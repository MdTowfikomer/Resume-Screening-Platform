package com.resumeai.dto.request;
// ═══════════════════════════════════════════════
//  MATCH REQUEST
// ═══════════════════════════════════════════════

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

class MatchRequest {

    @Data
    public static class TriggerMatch {
        @NotBlank
        private String resumeId;

        @NotBlank
        private String jobId;
    }

    @Data
    public static class BulkMatch {
        @NotBlank
        private String jobId;

        @Min(0)
        @Max(100)
        private double scoreThreshold = 60.0;
    }

    @Data
    public static class UpdateStatus {
        @NotNull
        private com.resumeai.model.MatchResult.MatchStatus status;

        private String recruiterNotes;
    }
}
