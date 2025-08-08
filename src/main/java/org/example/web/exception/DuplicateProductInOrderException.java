package org.example.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateProductInOrderException extends RuntimeException {
    public DuplicateProductInOrderException(UUID productId) {
        super("Повторяющийся товар в заказе: " + productId);
    }
}
