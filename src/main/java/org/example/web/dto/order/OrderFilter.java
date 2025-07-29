package org.example.web.dto.order;

import java.util.Optional;

public record OrderFilter(
        Optional<String> status,
        Optional<String> from,
        Optional<String> to
) {
}
