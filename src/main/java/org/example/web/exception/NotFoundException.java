package org.example.web.exception;

import java.util.UUID;

public class NotFoundException extends RuntimeException{

        public NotFoundException (String entity, UUID id) {
            super (entity +" с id " +id + " не существует");
        }

}
