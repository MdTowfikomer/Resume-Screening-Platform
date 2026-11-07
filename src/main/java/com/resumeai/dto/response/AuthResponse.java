package com.resumeai.dto.response;
// ═══════════════════════════════════════════════
//  AUTH RESPONSE
// ═══════════════════════════════════════════════

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class AuthResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private UserResponse user;
}
