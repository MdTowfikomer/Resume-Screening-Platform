package com.resumeai.controller;

import com.resumeai.constants.AppConstants;
import com.resumeai.dto.response.ApiResponse;
import com.resumeai.model.Resume;
import com.resumeai.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller exposing all resume-related endpoints.
 *
 * <pre>
 * POST   /api/v1/resumes/upload             Upload and parse a resume (PDF)
 * GET    /api/v1/resumes                    Paginated list of all resumes [ADMIN/RECRUITER]
 * GET    /api/v1/resumes/{id}               Get resume by ID
 * GET    /api/v1/resumes/by-email/{email}   Get resume by candidate email
 * GET    /api/v1/resumes/status/{status}    Filter resumes by processing status [ADMIN]
 * POST   /api/v1/resumes/search/skills      Search resumes by skill list
 * PUT    /api/v1/resumes/{id}               Update resume metadata
 * DELETE /api/v1/resumes/{id}               Soft-delete a resume
 * POST   /api/v1/resumes/{id}/reprocess     Re-trigger AI processing
 * GET    /api/v1/resumes/stats              Resume statistics [ADMIN/RECRUITER]
 * </pre>
 */
@RestController
@RequestMapping(AppConstants.RESUME_BASE)
@RequiredArgsConstructor
@Tag(name = "Resumes", description = "Resume upload, parsing, and management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ResumeController {

    private final ResumeService resumeService;

    // ── POST /upload ───────────────────────────────────────
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload a resume (PDF / DOCX)",
        description = "Accepts a PDF or DOCX file. Extracts text, queues AI processing via Kafka."
    )
    public ResponseEntity<ApiResponse<Resume>> uploadResume(
            @Parameter(description = "Resume file (PDF or DOCX, max 10 MB)",
                       content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file")          MultipartFile file,
            @RequestParam("candidateId")   String candidateId,
            @RequestParam("candidateEmail") String candidateEmail,
            @RequestParam("candidateName") String candidateName) {

        Resume resume = resumeService.uploadResume(file, candidateId, candidateEmail, candidateName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(resume,
                        "Resume uploaded successfully. AI processing queued asynchronously."));
    }

    // ── GET / (all resumes, paged) ─────────────────────────
    @GetMapping
    @Operation(summary = "Get all resumes (paginated) [RECRUITER / ADMIN]")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<Page<Resume>>> getAllResumes(
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "20")        int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return ResponseEntity.ok(ApiResponse.success(
                resumeService.getAllResumes(pageable), "Resumes retrieved"));
    }

    // ── GET /{id} ──────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(summary = "Get resume by ID")
    public ResponseEntity<ApiResponse<Resume>> getResumeById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(
                resumeService.getResumeById(id), "Resume retrieved"));
    }

    // ── GET /by-email/{email} ──────────────────────────────
    @GetMapping("/by-email/{email}")
    @Operation(summary = "Get resume by candidate email")
    public ResponseEntity<ApiResponse<Resume>> getResumeByEmail(@PathVariable String email) {
        return ResponseEntity.ok(ApiResponse.success(
                resumeService.getResumeByCandidateEmail(email), "Resume retrieved"));
    }

    // ── GET /status/{status} ───────────────────────────────
    @GetMapping("/status/{status}")
    @Operation(summary = "Filter resumes by processing status [ADMIN]")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Resume>>> getResumesByStatus(
            @PathVariable Resume.ProcessingStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                resumeService.getResumesByStatus(status, pageable), "Resumes retrieved"));
    }

    // ── POST /search/skills ────────────────────────────────
    @PostMapping("/search/skills")
    @Operation(summary = "Search resumes by skill list [RECRUITER / ADMIN]")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<List<Resume>>> searchBySkills(
            @RequestBody List<String> skills) {
        return ResponseEntity.ok(ApiResponse.success(
                resumeService.searchBySkills(skills), "Resumes matching skills retrieved"));
    }

    // ── PUT /{id} ──────────────────────────────────────────
    @PutMapping("/{id}")
    @Operation(summary = "Update resume metadata")
    public ResponseEntity<ApiResponse<Resume>> updateResume(
            @PathVariable String id,
            @RequestBody Resume updated) {
        return ResponseEntity.ok(ApiResponse.success(
                resumeService.updateResume(id, updated), "Resume updated"));
    }

    // ── DELETE /{id} ───────────────────────────────────────
    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a resume")
    public ResponseEntity<ApiResponse<Void>> deleteResume(@PathVariable String id) {
        resumeService.deleteResume(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Resume deleted"));
    }

    // ── POST /{id}/reprocess ───────────────────────────────
    @PostMapping("/{id}/reprocess")
    @Operation(summary = "Re-trigger AI processing for a resume [ADMIN]")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Resume>> reprocessResume(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(
                resumeService.reprocessResume(id), "Reprocessing triggered"));
    }

    // ── GET /stats ─────────────────────────────────────────
    @GetMapping("/stats")
    @Operation(summary = "Resume processing statistics [RECRUITER / ADMIN]")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<?>> getStats() {
        var stats = java.util.Map.of(
                "totalActiveResumes", resumeService.getResumeCount());
        return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved"));
    }
}
