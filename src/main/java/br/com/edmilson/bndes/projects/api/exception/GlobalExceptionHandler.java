package br.com.edmilson.bndes.projects.api.exception;

import br.com.edmilson.bndes.projects.api.messages.ApiMessages;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(
      MethodArgumentNotValidException ex,
      HttpServletRequest request
  ) {
    List<Map<String, String>> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(this::mapFieldError)
            .toList();

    Map<String, Object> details = new LinkedHashMap<>();
    details.put("fieldErrors", fieldErrors);

    return build(HttpStatus.BAD_REQUEST, ApiMessages.VALIDATION_ERROR, request.getRequestURI(), details);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleInvalidJson(
      HttpMessageNotReadableException ex,
      HttpServletRequest request
  ) {
   
    String cause = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : null;
    if (cause != null && cause.length() > 200) {
      cause = cause.substring(0, 200) + "...";
    }

    Map<String, Object> details = new LinkedHashMap<>();
    if (cause != null && !cause.isBlank()) {
      details.put("cause", cause);
    }

    return build(HttpStatus.BAD_REQUEST, ApiMessages.INVALID_JSON_BODY, request.getRequestURI(), details);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ApiError> handleValidationException(
      ValidationException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiError> handleUnauthorized(
      UnauthorizedException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI(), null);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiError> handleForbidden(
      ForbiddenException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI(), null);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(
      ResourceNotFoundException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> handleConflict(
      DataIntegrityViolationException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.CONFLICT, ApiMessages.RESOURCE_ALREADY_EXISTS, request.getRequestURI(), null);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiError> handleResponseStatus(
      ResponseStatusException ex,
      HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    String msg = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
    return build(status, msg, request.getRequestURI(), null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnexpected(
      Exception ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, ApiMessages.UNEXPECTED_ERROR, request.getRequestURI(), null);
  }

  private Map<String, String> mapFieldError(FieldError fe) {
    Map<String, String> m = new LinkedHashMap<>();
    m.put("field", fe.getField());
    m.put("message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value.");
    return m;
  }

  private ResponseEntity<ApiError> build(
      HttpStatus status,
      String message,
      String path,
      Map<String, Object> details
  ) {
    ApiError body =
        new ApiError(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            (details == null || details.isEmpty()) ? null : details
        );

    return ResponseEntity.status(status).body(body);
  }
}
