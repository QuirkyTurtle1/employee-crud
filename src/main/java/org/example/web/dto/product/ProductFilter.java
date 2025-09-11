package org.example.web.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Optional;

public record ProductFilter(
        @Schema(description = "Max price", example = "100.00")
        String name,

        @Schema(description = "Min price", example = "10.00")
        BigDecimal priceMin,

        @Schema(description = "Max price", example = "100.00")
        BigDecimal priceMax
) {
}
