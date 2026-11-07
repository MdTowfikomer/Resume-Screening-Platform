package com.resumeai.service.impl;

import com.resumeai.exception.DuplicateResourceException;
import com.resumeai.exception.ResourceNotFoundException;
import com.resumeai.model.User;
import com.resumeai.repository.mongo.UserRepository;
import com.resumeai.security.JwtTokenProvider;
import com.resumeai.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository      userRepository;
    private final PasswordEncoder     passwordEncoder;
    private final JwtTokenProvider    jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService  userDetailsService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already taken: " + request.username());
        }

        User user = User.builder()
                .email(request.email())
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .companyName(request.companyName())
                .roles(Set.of(request.role() != null ? request.role() : User.Role.ROLE_CANDIDATE))
                .isActive(true)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("User registered: {} [{}]", user.getEmail(), user.getRoles());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtTokenProvider.generateToken(userDetails);

        return new AuthResponse(token, "Bearer", 86400L,
                user.getId(), user.getEmail(), user.getUsername(), user.getRoles());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.usernameOrEmail(), request.password()));

        User user = userRepository.findByEmailOrUsername(
                        request.usernameOrEmail(), request.usernameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtTokenProvider.generateToken(userDetails);

        log.info("User logged in: {}", user.getEmail());
        return new AuthResponse(token, "Bearer", 86400L,
                user.getId(), user.getEmail(), user.getUsername(), user.getRoles());
    }

    @Override
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
