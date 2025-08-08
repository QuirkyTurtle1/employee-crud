package org.example.web.dto.orderProduct;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductRequest {

    UUID productId;

    @Min(1)
    int quantity;
}
