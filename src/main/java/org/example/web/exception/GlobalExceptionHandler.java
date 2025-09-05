package org.example.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.web.dto.ApiError;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiError> respond(HttpStatus status, String code, String msg,
                                             HttpServletRequest req, Throwable ex) {
        String head = status.value() + " " + status.getReasonPhrase()
                + ": " + req.getMethod() + " " + req.getRequestURI();

        if (status.is5xxServerError()) log.error("{} -> {}", head, msg, ex);
        else                           log.warn("{} -> {}", head, msg);

        String requestId = org.slf4j.MDC.get("requestId"); // позже появится
        ApiError body = new ApiError(
                java.time.Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                req.getRequestURI(),
                code,
                msg,
                requestId
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler({
            DuplicateEmailException.class,
            DuplicateProductNameException.class,
            DuplicateProductInOrderException.class,
            ProductInUseException.class,
            org.springframework.dao.DataIntegrityViolationException.class
    })
    public ResponseEntity<ApiError> handleConflict(Exception ex, HttpServletRequest req) {
        return respond(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), req, ex);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return respond(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req, ex);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(org.springframework.web.bind.MethodArgumentNotValidException ex,
                                                     HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .findFirst().orElse("Validation error");
        return respond(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", msg, req, ex);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(jakarta.validation.ConstraintViolationException ex,
                                                     HttpServletRequest req) {
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .findFirst().orElse("Constraint violation");
        return respond(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION", msg, req, ex);
    }

    @ExceptionHandler({
            org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(Exception ex, HttpServletRequest req) {
        String msg = (ex instanceof org.springframework.http.converter.HttpMessageNotReadableException jm
                && jm.getMostSpecificCause() != null)
                ? "Malformed JSON: " + jm.getMostSpecificCause().getMessage()
                : "Bad request";
        return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", msg, req, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest req) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error", req, ex);
    }
}

