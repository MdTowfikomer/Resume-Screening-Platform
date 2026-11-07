package com.resumeai.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

// ═══════════════════════════════════════════════
//  GENERIC API WRAPPER
// ═══════════════════════════════════════════════

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int statusCode;
    private Instant timestamp;
    private String path;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true).message(message).data(data)
                .statusCode(200).timestamp(Instant.now()).build();
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true).message(message).data(data)
                .statusCode(201).timestamp(Instant.now()).build();
    }

    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false).message(message)
                .statusCode(statusCode).timestamp(Instant.now()).build();
    }
}
