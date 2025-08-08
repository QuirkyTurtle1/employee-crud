package org.example.web.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProductInUseException extends RuntimeException{
    public ProductInUseException(UUID id) {
        super ("Продукт  " +id + " уже используется в заказах");
    }
}
