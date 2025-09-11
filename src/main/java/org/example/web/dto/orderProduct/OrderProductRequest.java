package org.example.web.dto.orderProduct;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductRequest {

    @Schema(description = "Product UUID", example = "321e4567-e89b-12d3-a456-426614174000")
    @NotNull
    UUID productId;

    @Schema(description = "Quantity of the product", example = "2")
    @Min(1)
    int quantity;
}
