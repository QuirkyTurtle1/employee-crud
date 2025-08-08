package org.example.web.dto.order;

import jakarta.validation.Valid;
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

    @NotNull
    private UUID clientId;

    @NotNull
    private OrderStatus status;

    List<@Valid OrderProductRequest> products;
}
