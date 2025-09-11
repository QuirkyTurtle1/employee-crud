package org.example.web.dto.product;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    @Schema(description = "Product UUID", example = "123e4567-e89b-12d3-a456-426614174000", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Product name", example = "Book")
    private String name;

    @Schema(description = "Product description", example = "A thrilling science fiction novel")
    private String description;

    @Schema(description = "Price per unit of the product", example = "19.99")
    private BigDecimal price;

}
