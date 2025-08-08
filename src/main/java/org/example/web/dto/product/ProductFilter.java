package org.example.web.dto.product;

import java.math.BigDecimal;
import java.util.Optional;

public record ProductFilter(
        Optional<String> name,
        Optional<BigDecimal> priceMin,
        Optional<BigDecimal> priceMax
) {
}
