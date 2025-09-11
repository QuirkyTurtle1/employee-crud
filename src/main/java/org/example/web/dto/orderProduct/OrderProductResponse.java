package org.example.web.dto.orderProduct;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProductResponse {
    @Schema(description = "Product UUID", example = "123e4567-e89b-12d3-a456-426614174000", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID productId;

    @Schema(description = "Product name", example = "Book")
    private String name;

    @Schema(description = "Quantity of this product in the order", example = "2")
    int quantity;

    @Schema(description = "Price per unit of the product", example = "19.99")
    BigDecimal price;
}
