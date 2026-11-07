package com.resumeai.service;

import com.resumeai.model.User;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    User getCurrentUser(String email);

    record RegisterRequest(String email, String username, String password,
                           String firstName, String lastName,
                           String phone, String companyName, User.Role role) {}

    record LoginRequest(String usernameOrEmail, String password) {}

    record AuthResponse(String accessToken, String tokenType, Long expiresIn,
                        String userId, String email, String username,
                        java.util.Set<User.Role> roles) {}
}
