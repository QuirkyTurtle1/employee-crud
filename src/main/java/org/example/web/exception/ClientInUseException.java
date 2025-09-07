package org.example.web.exception;

import java.util.UUID;

public class ClientInUseException extends RuntimeException {
    public ClientInUseException(UUID id) {
        super("Клиент " + id + " имеет заказы");
    }
}
