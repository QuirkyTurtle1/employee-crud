package org.example.web.exception;

import java.util.UUID;

public class NotFoundException extends RuntimeException{

        public NotFoundException (UUID id) {
            super ("Сотрудник с id " +id + " не существует");
        }

}
