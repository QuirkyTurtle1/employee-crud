package org.example.web.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.web.dto.orderProduct.OrderProductResponse;
import org.example.web.model.OrderStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    @Schema(description = "Order UUID", example = "123e4567-e89b-12d3-a456-426614174000", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Timestamp when the order was created (ISO-8601)",
            example = "2025-09-11T14:30:00Z", format = "date-time")
    private LocalDateTime createdAt;

    @Schema(description = "Current order status", example = "NEW", implementation = OrderStatus.class)
    private OrderStatus status;

    @Schema(description = "Client UUID who placed the order", example = "321e4567-e89b-12d3-a456-426614174000")
    private UUID clientId;

    @Schema(description = "List of products in the order",
            example = "[{\"productId\": \"111e4567-e89b-12d3-a456-426614174000\", \"name\": \"Book\", \"quantity\": 2, \"price\": 19.99}]")
    private List<OrderProductResponse> items;

    @Schema(description = "Total number of items in the order", example = "3")
    private Integer itemsTotal;
}
