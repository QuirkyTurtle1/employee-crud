package org.example.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


public class DuplicateProductNameException extends RuntimeException{
    public DuplicateProductNameException(String productName) {
        super ("Продукт с названием " +productName + " уже существует");
    }
}
