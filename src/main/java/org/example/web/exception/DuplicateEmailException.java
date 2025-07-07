package org.example.web.exception;

public class DuplicateEmailException extends RuntimeException{
    public DuplicateEmailException (String email) {
        super ("Сотрудник с email + " +email + " уже существует");
    }
}
