package org.example.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.web.model.OrderStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private UUID id;
    private Instant createdAt;
    private OrderStatus status;
    private UUID clientID;
}
