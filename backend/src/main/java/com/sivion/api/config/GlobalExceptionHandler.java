package com.sivion.api.config;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequest(Exception ex) {
        String message = ex instanceof MethodArgumentNotValidException validation
                ? validation.getBindingResult().getFieldErrors().stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .collect(Collectors.joining("; "))
                : safeMessage(ex);
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(NoSuchElementException ex) {
        return error(HttpStatus.NOT_FOUND, safeMessage(ex));
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError conflict(IllegalStateException ex) {
        return error(HttpStatus.CONFLICT, safeMessage(ex));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError dataConflict() {
        return error(HttpStatus.CONFLICT, "The requested change conflicts with existing data.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError unexpected(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred.");
    }

    private ApiError error(HttpStatus status, String message) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message);
    }

    private String safeMessage(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank() ? "Invalid request." : ex.getMessage();
    }

    public record ApiError(Instant timestamp, int status, String error, String message) {}
}
