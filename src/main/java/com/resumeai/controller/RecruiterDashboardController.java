package com.resumeai.controller;

import com.resumeai.constants.AppConstants;
import com.resumeai.dto.response.ApiResponse;
import com.resumeai.service.impl.RecruiterDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Recruiter Dashboard — aggregated analytics and stats.
 *
 * <pre>
 * GET /api/v1/recruiter/dashboard    Recruiter stats: jobs, applications, top candidates
 * </pre>
 */
@RestController
@RequestMapping(AppConstants.RECRUITER_BASE)
@RequiredArgsConstructor
@Tag(name = "Recruiter Dashboard", description = "Recruiter analytics and hiring pipeline overview")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
public class RecruiterDashboardController {

    private final RecruiterDashboardService dashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get recruiter dashboard statistics and top candidates")
    public ResponseEntity<ApiResponse<RecruiterDashboardService.DashboardStats>> getDashboard(
            @AuthenticationPrincipal UserDetails currentUser) {

        var stats = dashboardService.getDashboardStats(currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard data retrieved"));
    }
}
