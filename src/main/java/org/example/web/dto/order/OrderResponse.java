package org.example.web.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.web.dto.orderProduct.OrderProductResponse;
import org.example.web.model.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private UUID id;
    private Instant createdAt;
    private OrderStatus status;
    private UUID clientId;
    private List<OrderProductResponse> items;
    private Integer itemsTotal;
}
