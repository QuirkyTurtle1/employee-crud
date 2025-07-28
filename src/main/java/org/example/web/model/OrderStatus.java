package org.example.web.model;

import lombok.Getter;

@Getter
public enum OrderStatus {
    NEW("Новый"),
    PROCESSING("В процессе"),
    COMPLETED("Выполнен"),
    CANCELED("Отменен");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
