package org.example.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standard error body for API responses")
public record ApiError (
        @Schema(description = "When error occurred (UTC)", example = "2025-09-08T15:21:30.123Z")
        Instant timestamp,
        @Schema(description = "HTTP status", example = "400")
        int status,
        @Schema(description = "Reason phrase of HTTP status", example = "Bad Request")
        String error,
        @Schema(description = "Request path", example = "/api/products")
        String path,
        @Schema(description = "Machine-readable error code", example = "VALIDATION_ERROR")
        String code,
        @Schema(description = "Human-readable message", example = "name must not be blank")
        String message,
        @Schema(description = "Correlation id for logs", example = "f0a9e6cf-5b3a-41d9-8b64-8b2a8f0a1e36")
        String requestId
) {}