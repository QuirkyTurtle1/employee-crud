package org.example.web.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.web.dto.orderProduct.OrderProductRequest;
import org.example.web.model.OrderStatus;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @Schema(description = "Client UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotNull
    private UUID clientId;

    @Schema(description = "Current order status", example = "NEW", implementation = OrderStatus.class)
    @NotNull
    private OrderStatus status;

    @NotEmpty
    @Schema(description = "List of products in the order",
            example = "[{\"productId\": \"321e4567-e89b-12d3-a456-426614174000\", \"quantity\": 2}]")
    List<@Valid OrderProductRequest> products;
}
