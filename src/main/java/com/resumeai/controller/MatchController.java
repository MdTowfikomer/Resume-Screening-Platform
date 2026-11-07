package com.resumeai.controller;

import com.resumeai.constants.AppConstants;
import com.resumeai.dto.response.ApiResponse;
import com.resumeai.model.MatchResult;
import com.resumeai.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for AI-powered job matching.
 *
 * <pre>
 * POST   /api/v1/matches/trigger          Trigger a single resume-to-job match
 * POST   /api/v1/matches/bulk/{jobId}     Bulk-match all processed resumes to a job [ASYNC]
 * GET    /api/v1/matches/{id}            Get a specific match result
 * GET    /api/v1/matches/by-job/{jobId}  All matches for a job (sorted by score DESC)
 * GET    /api/v1/matches/by-resume/{resumeId} All job matches for a resume
 * GET    /api/v1/matches/top/{jobId}     Top candidates above score threshold
 * PATCH  /api/v1/matches/{id}/status     Update match status (shortlist, reject, hire)
 * DELETE /api/v1/matches/{id}           Delete a match result
 * </pre>
 */
@RestController
@RequestMapping(AppConstants.MATCH_BASE)
@RequiredArgsConstructor
@Tag(name = "Job Matching", description = "AI-powered resume-to-job matching and scoring APIs")
@SecurityRequirement(name = "bearerAuth")
public class MatchController {

    private final MatchService matchService;

    // ── POST /trigger ──────────────────────────────────────
    @PostMapping("/trigger")
    @Operation(summary = "Trigger AI match between a resume and a job posting")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<MatchResult>> triggerMatch(
            @Valid @RequestBody TriggerMatchRequest request) {
        MatchResult result = matchService.triggerMatch(request.getResumeId(), request.getJobId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(result, "Match computed successfully"));
    }

    // ── POST /bulk/{jobId} ─────────────────────────────────
    @PostMapping("/bulk/{jobId}")
    @Operation(summary = "Bulk-match all processed resumes to a job [async, RECRUITER]")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> triggerBulkMatch(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "60.0") double scoreThreshold) {

        matchService.triggerBulkMatch(jobId, scoreThreshold);
        return ResponseEntity.accepted().body(ApiResponse.success(null,
                "Bulk matching started asynchronously for job: " + jobId));
    }

    // ── GET /{id} ──────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(summary = "Get a specific match result by ID")
    public ResponseEntity<ApiResponse<MatchResult>> getMatchById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(
                matchService.getMatchById(id), "Match result retrieved"));
    }

    // ── GET /by-job/{jobId} ────────────────────────────────
    @GetMapping("/by-job/{jobId}")
    @Operation(summary = "Get all matches for a job posting, sorted by score DESC")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<Page<MatchResult>>> getMatchesByJob(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                matchService.getMatchesByJob(jobId, pageable), "Matches retrieved"));
    }

    // ── GET /by-resume/{resumeId} ──────────────────────────
    @GetMapping("/by-resume/{resumeId}")
    @Operation(summary = "Get all job matches for a resume")
    public ResponseEntity<ApiResponse<Page<MatchResult>>> getMatchesByResume(
            @PathVariable String resumeId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                matchService.getMatchesByResume(resumeId, pageable), "Matches retrieved"));
    }

    // ── GET /top/{jobId} ───────────────────────────────────
    @GetMapping("/top/{jobId}")
    @Operation(summary = "Get top candidates above score threshold for a job")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<List<MatchResult>>> getTopCandidates(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "70.0") double threshold) {

        return ResponseEntity.ok(ApiResponse.success(
                matchService.getTopMatchesForJob(jobId, threshold),
                "Top candidates retrieved"));
    }

    // ── PATCH /{id}/status ─────────────────────────────────
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update match status (SHORTLISTED / REJECTED / HIRED) [RECRUITER]")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<MatchResult>> updateMatchStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        MatchResult updated = matchService.updateMatchStatus(
                id, request.getStatus(), request.getRecruiterNotes(), currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(updated, "Match status updated"));
    }

    // ── DELETE /{id} ───────────────────────────────────────
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a match result [ADMIN]")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMatch(@PathVariable String id) {
        matchService.deleteMatch(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Match result deleted"));
    }

    // ── Request DTOs ───────────────────────────────────────
    @Data
    public static class TriggerMatchRequest {
        @NotBlank private String resumeId;
        @NotBlank private String jobId;
    }

    @Data
    public static class UpdateStatusRequest {
        @NotNull private MatchResult.MatchStatus status;
        private String recruiterNotes;
    }
}
