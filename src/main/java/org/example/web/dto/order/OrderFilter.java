package org.example.web.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public record OrderFilter(

        @Schema(description = "Filter by order status",
                example = "NEW", allowableValues = {"NEW", "PAID", "CANCELLED", "SHIPPED"})
        String status,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @Schema(description = "Created from, ISO-8601",
                example = "2025-09-10T00:00:00Z", format = "date-time")
        LocalDateTime from,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @Schema(description = "Created to (inclusive), ISO-8601",
                example = "2025-09-11T23:59:59Z", format = "date-time")
        LocalDateTime to,

        @Schema(description = "Filter by containing product ID",
                example = "c1f9b4e6-7c2d-4f89-b05d-7f3a8b9c1a2f")
        UUID productId
) {
}
