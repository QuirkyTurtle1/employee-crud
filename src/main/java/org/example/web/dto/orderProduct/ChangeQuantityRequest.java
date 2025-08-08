package org.example.web.dto.orderProduct;

import jakarta.validation.constraints.Min;

public record ChangeQuantityRequest(@Min(1) int quantity) {
}
