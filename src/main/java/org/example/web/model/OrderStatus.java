package org.example.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public enum OrderStatus {
    @Schema(description = "Order is new and not processed yet")
    NEW("Новый"),
    @Schema(description = "Order is in processing")
    PROCESSING("В процессе"),
    @Schema(description = "Order is completed")
    COMPLETED("Выполнен"),
    @Schema(description = "Order is cancelled")
    CANCELED("Отменен");

    @Schema(description = "Localized status description", example = "Новый")
    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
