package com.resumeai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Set;

/**
 * Application user — supports CANDIDATE, RECRUITER, and ADMIN roles.
 */
@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String username;

    private String password;

    @Field("first_name")
    private String firstName;

    @Field("last_name")
    private String lastName;

    @Field("phone")
    private String phone;

    @Field("company_name")
    private String companyName;

    private Set<Role> roles;

    @Field("is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Field("email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Field("last_login_at")
    private Instant lastLoginAt;

    @Field("profile_picture_url")
    private String profilePictureUrl;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    public enum Role {ROLE_ADMIN, ROLE_RECRUITER, ROLE_CANDIDATE}
}
