package org.example.web.dto.orderProduct;

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
    private UUID productId;
    private String name;
    int quantity;
    BigDecimal price;
}
