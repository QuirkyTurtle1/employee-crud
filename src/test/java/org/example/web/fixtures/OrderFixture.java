package org.example.web.fixtures;

import org.example.web.model.Client;
import org.example.web.model.Order;
import org.example.web.model.Order.OrderBuilder;
import org.example.web.model.OrderStatus;
import org.example.web.model.Product;

import java.time.LocalDateTime;

public class OrderFixture {
    public static OrderBuilder defaultOrder(Client client) {
        return Order.builder()
                .client(client)
                .status(OrderStatus.NEW)
                .createdAt(LocalDateTime.now());
    }

    public static Order readyOrder(Client client) {
        return defaultOrder(client).build();
    }
}
