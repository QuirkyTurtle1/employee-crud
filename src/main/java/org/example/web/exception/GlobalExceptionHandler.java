package org.example.web.exception;

import jakarta.validation.ConstraintViolationException;
import org.example.web.dto.ApiRecord;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiRecord> handleDuplicate(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiRecord(ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiRecord> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiRecord(ex.getMessage()));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiRecord> handleValidation(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiRecord(errorMessage));
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiRecord> handleConstraintViolation(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .findFirst()
                .orElse("Constraint violation");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiRecord(errorMessage));
    }


}
