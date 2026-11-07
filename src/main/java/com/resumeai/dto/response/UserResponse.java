package com.resumeai.dto.response;
// ═══════════════════════════════════════════════
//  USER RESPONSE
// ═══════════════════════════════════════════════

import com.fasterxml.jackson.annotation.JsonInclude;
import com.resumeai.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class UserResponse {
    private String id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String companyName;
    private java.util.Set<User.Role> roles;
    private Boolean isActive;
    private Instant createdAt;
}
