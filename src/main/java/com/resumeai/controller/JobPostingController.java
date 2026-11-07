package com.resumeai.controller;

import com.resumeai.constants.AppConstants;
import com.resumeai.dto.response.ApiResponse;
import com.resumeai.model.JobPosting;
import com.resumeai.service.JobPostingService;
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

import java.time.Instant;
import java.util.List;

/**
 * REST controller for Job Posting management.
 *
 * <pre>
 * POST   /api/v1/jobs                     Create a new job posting [RECRUITER]
 * GET    /api/v1/jobs                     List all active jobs (public)
 * GET    /api/v1/jobs/{id}               Get job by ID (public)
 * GET    /api/v1/jobs/search             Full-text search on job title
 * GET    /api/v1/jobs/recruiter/mine     All jobs by authenticated recruiter
 * PUT    /api/v1/jobs/{id}              Update job [RECRUITER - owner]
 * DELETE /api/v1/jobs/{id}             Close / delete job [RECRUITER - owner]
 * POST   /api/v1/jobs/search/skills     Find jobs requiring given skills
 * </pre>
 */
@RestController
@RequestMapping(AppConstants.JOB_BASE)
@RequiredArgsConstructor
@Tag(name = "Job Postings", description = "Job creation, search, and management APIs")
public class JobPostingController {

    private final JobPostingService jobPostingService;

    // ── POST / (create) ────────────────────────────────────
    @PostMapping
    @Operation(summary = "Create a new job posting [RECRUITER]",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<JobPosting>> createJob(
            @Valid @RequestBody CreateJobRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        JobPosting job = mapToEntity(request);
        JobPosting saved = jobPostingService.createJob(job, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(saved, "Job posting created successfully"));
    }

    // ── GET / (all active, public) ─────────────────────────
    @GetMapping
    @Operation(summary = "List all active job postings (public, paginated)")
    public ResponseEntity<ApiResponse<Page<JobPosting>>> getAllActiveJobs(
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "20")        int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return ResponseEntity.ok(ApiResponse.success(
                jobPostingService.getAllActiveJobs(pageable), "Jobs retrieved"));
    }

    // ── GET /{id} (public) ─────────────────────────────────
    @GetMapping("/{id}")
    @Operation(summary = "Get a job posting by ID (public)")
    public ResponseEntity<ApiResponse<JobPosting>> getJobById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(
                jobPostingService.getJobById(id), "Job retrieved"));
    }

    // ── GET /search ────────────────────────────────────────
    @GetMapping("/search")
    @Operation(summary = "Full-text search jobs by title keyword")
    public ResponseEntity<ApiResponse<Page<JobPosting>>> searchJobs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                jobPostingService.searchJobs(keyword, pageable), "Search results"));
    }

    // ── GET /recruiter/mine ────────────────────────────────
    @GetMapping("/recruiter/mine")
    @Operation(summary = "Get all job postings by the authenticated recruiter",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<Page<JobPosting>>> getMyJobs(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                jobPostingService.getJobsByRecruiter(currentUser.getUsername(), pageable),
                "Your job postings retrieved"));
    }

    // ── PUT /{id} ──────────────────────────────────────────
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing job posting [RECRUITER - owner]",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<JobPosting>> updateJob(
            @PathVariable String id,
            @Valid @RequestBody CreateJobRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        JobPosting updated = jobPostingService.updateJob(
                id, mapToEntity(request), currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(updated, "Job posting updated"));
    }

    // ── DELETE /{id} ───────────────────────────────────────
    @DeleteMapping("/{id}")
    @Operation(summary = "Close / delete a job posting [RECRUITER - owner]",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails currentUser) {

        jobPostingService.deleteJob(id, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Job posting closed"));
    }

    // ── POST /search/skills ────────────────────────────────
    @PostMapping("/search/skills")
    @Operation(summary = "Find active jobs that require given skills")
    public ResponseEntity<ApiResponse<List<JobPosting>>> findJobsBySkills(
            @RequestBody List<String> skills) {
        return ResponseEntity.ok(ApiResponse.success(
                jobPostingService.findJobsBySkills(skills), "Matching jobs retrieved"));
    }

    // ── Mapping helper ─────────────────────────────────────
    private JobPosting mapToEntity(CreateJobRequest r) {
        return JobPosting.builder()
                .title(r.getTitle())
                .description(r.getDescription())
                .companyName(r.getCompanyName())
                .location(r.getLocation())
                .remoteAllowed(r.getRemoteAllowed())
                .employmentType(r.getEmploymentType())
                .experienceLevel(r.getExperienceLevel())
                .minExperienceYears(r.getMinExperienceYears())
                .maxExperienceYears(r.getMaxExperienceYears())
                .salaryMin(r.getSalaryMin())
                .salaryMax(r.getSalaryMax())
                .salaryCurrency(r.getSalaryCurrency() != null ? r.getSalaryCurrency() : "USD")
                .requiredSkills(r.getRequiredSkills())
                .preferredSkills(r.getPreferredSkills())
                .requiredEducation(r.getRequiredEducation())
                .responsibilities(r.getResponsibilities())
                .benefits(r.getBenefits())
                .tags(r.getTags())
                .expiresAt(r.getExpiresAt())
                .build();
    }

    // ── Request DTO ────────────────────────────────────────
    @Data
    public static class CreateJobRequest {
        @NotBlank @Size(max = 200)
        private String title;
        @NotBlank
        private String description;
        @NotBlank
        private String companyName;
        private String location;
        private Boolean remoteAllowed;
        @NotNull
        private JobPosting.EmploymentType employmentType;
        @NotNull
        private JobPosting.ExperienceLevel experienceLevel;
        @Min(0) @Max(40)
        private Integer minExperienceYears;
        @Min(0) @Max(40)
        private Integer maxExperienceYears;
        private Long salaryMin;
        private Long salaryMax;
        private String salaryCurrency;
        @NotEmpty
        private List<String> requiredSkills;
        private List<String> preferredSkills;
        private String requiredEducation;
        private List<String> responsibilities;
        private List<String> benefits;
        private List<String> tags;
        private Instant expiresAt;
    }
}
