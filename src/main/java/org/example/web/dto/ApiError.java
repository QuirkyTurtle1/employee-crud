package org.example.web.dto;

import java.time.Instant;

public record ApiError (
        Instant timestamp,
        int status,
        String error,
        String path,
        String code,
        String message,
        String requestId
) {}