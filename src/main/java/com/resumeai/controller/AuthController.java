package com.resumeai.controller;

import com.resumeai.dto.response.ApiResponse;
import com.resumeai.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static com.resumeai.constants.AppConstants.AUTH_BASE;

@RestController
@RequestMapping(AUTH_BASE)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login APIs")
public class AuthController {

    private final AuthService authService;

    // ── POST /api/v1/auth/register ─────────────────────────
    @PostMapping("/register")
    @Operation(summary = "Register a new user (candidate or recruiter)")
    public ResponseEntity<ApiResponse<AuthResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto request) {

        var result = authService.register(new AuthService.RegisterRequest(
                request.getEmail(), request.getUsername(), request.getPassword(),
                request.getFirstName(), request.getLastName(),
                request.getPhone(), request.getCompanyName(),
                com.resumeai.model.User.Role.valueOf(
                        request.getRole() != null ? request.getRole() : "ROLE_CANDIDATE")));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(toDto(result), "User registered successfully"));
    }

    // ── POST /api/v1/auth/login ────────────────────────────
    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive JWT access token")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request) {

        var result = authService.login(
                new AuthService.LoginRequest(request.getUsernameOrEmail(), request.getPassword()));

        return ResponseEntity.ok(ApiResponse.success(toDto(result), "Login successful"));
    }

    // ── GET /api/v1/auth/me ────────────────────────────────
    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<?>> getCurrentUser(
            @AuthenticationPrincipal
            org.springframework.security.core.userdetails.UserDetails userDetails) {

        var user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(user, "Profile retrieved"));
    }

    // ── Helper ─────────────────────────────────────────────
    private AuthResponseDto toDto(AuthService.AuthResponse r) {
        return new AuthResponseDto(r.accessToken(), r.tokenType(), r.expiresIn(),
                r.userId(), r.email(), r.username(), r.roles());
    }

    // ── Request / Response DTOs ───────────────────────────

    @Data
    public static class RegisterRequestDto {
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 3, max = 50)
        private String username;
        @NotBlank @Size(min = 8, max = 100)
        private String password;
        @NotBlank
        private String firstName;
        @NotBlank
        private String lastName;
        private String phone;
        private String companyName;
        private String role; // ROLE_CANDIDATE | ROLE_RECRUITER | ROLE_ADMIN
    }

    @Data
    public static class LoginRequestDto {
        @NotBlank
        private String usernameOrEmail;
        @NotBlank
        private String password;
    }

    public record AuthResponseDto(
            String accessToken, String tokenType, Long expiresIn,
            String userId, String email, String username,
            Set<com.resumeai.model.User.Role> roles) {}
}
