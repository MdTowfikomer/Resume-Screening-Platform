package com.resumeai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ═══════════════════════════════════════════════
//  AUTH REQUESTS
// ═══════════════════════════════════════════════

public class AuthRequest {

    @Data
    public static class Register {
        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank
        @Size(min = 8, max = 100)
        private String password;

        @NotBlank
        private String firstName;

        @NotBlank
        private String lastName;

        private String phone;
        private String companyName;

        @NotNull
        private com.resumeai.model.User.Role role;
    }

    @Data
    public static class Login {
        @NotBlank
        private String usernameOrEmail;

        @NotBlank
        private String password;
    }
}
