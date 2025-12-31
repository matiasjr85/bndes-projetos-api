package br.com.edmilson.bndes.projects.api.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    return ResponseEntity.status(status).body(
        new ApiError(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            ex.getReason(),
            req.getRequestURI(),
            null
        )
    );
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    Map<String, Object> details = new LinkedHashMap<>();
    Map<String, String> fieldErrors = new LinkedHashMap<>();

    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(fe.getField(), fe.getDefaultMessage());
    }
    details.put("fields", fieldErrors);

    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status).body(
        new ApiError(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            "Validation error.",
            req.getRequestURI(),
            details
        )
    );
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status).body(
        new ApiError(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            "Invalid JSON body.",
            req.getRequestURI(),
            null
        )
    );
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.FORBIDDEN;
    return ResponseEntity.status(status).body(
        new ApiError(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            "Access denied.",
            req.getRequestURI(),
            null
        )
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(status).body(
        new ApiError(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            "Unexpected error.",
            req.getRequestURI(),
            null
        )
    );
  }
}
