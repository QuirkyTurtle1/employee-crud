package org.example.web.dto.product;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @Schema(description = "Product name", example = "Book")
    @NotBlank
    private String name;

    @Schema(description = "Product description", example = "A thrilling science fiction novel")
    @NotBlank
    private String description;

    @Schema(description = "Price per unit of the product", example = "19.99")
    @NotNull
    private BigDecimal price;
}
