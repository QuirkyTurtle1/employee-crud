package org.example.web.dto.order;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record OrderFilter(
        Optional<String> status,
        Optional<Instant> from,
        Optional<Instant> to,
        Optional<UUID> productId
) {
}
