package br.com.edmilson.bndes.projects.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static br.com.edmilson.bndes.projects.api.messages.ApiMessages.*;
import static br.com.edmilson.bndes.projects.api.messages.LogMessages.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    
    log.warn("ResponseStatusException status={} path={} reason={}", status.value(), req.getRequestURI(), ex.getReason());

    return build(status, ex.getReason(), req.getRequestURI(), null);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    Map<String, String> fieldErrors = new LinkedHashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(fe.getField(), fe.getDefaultMessage());
    }

    Map<String, Object> details = new LinkedHashMap<>();
    details.put("fields", fieldErrors);

    log.warn("{} path={} fields={}", VALIDATION_FAILED, req.getRequestURI(), fieldErrors);

    return build(HttpStatus.BAD_REQUEST, VALIDATION_ERROR, req.getRequestURI(), details);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
    log.warn("Error - Invalid JSON body. path={}", req.getRequestURI());
    return build(HttpStatus.BAD_REQUEST, INVALID_JSON_BODY, req.getRequestURI(), null);
  }
  
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ApiError> handleBusinessValidation(ValidationException ex, HttpServletRequest req) {
    log.warn("{} path={} message={}", VALIDATION_FAILED, req.getRequestURI(), ex.getMessage());
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI(), null);
  }
  
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
    log.warn("{} path={} message={}", UNAUTHORIZED_ACCESS, req.getRequestURI(), ex.getMessage());
    return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req.getRequestURI(), null);
  }
  
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
    log.warn("{} path={} message={}", ACCESS_FORBIDDEN, req.getRequestURI(), ex.getMessage());
    return build(HttpStatus.FORBIDDEN, ex.getMessage(), req.getRequestURI(), null);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
    log.warn("{} path={}", UNAUTHORIZED_ACCESS, req.getRequestURI());
    return build(HttpStatus.UNAUTHORIZED, UNAUTHORIZED, req.getRequestURI(), null);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
    log.warn("{} path={}", ACCESS_FORBIDDEN, req.getRequestURI());
    return build(HttpStatus.FORBIDDEN, ACCESS_DENIED, req.getRequestURI(), null);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
    log.warn("Error - Data integrity violation. path={}", req.getRequestURI());
    return build(HttpStatus.CONFLICT, RESOURCE_ALREADY_EXISTS, req.getRequestURI(), null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {    
    log.error("Unexpected error on path={}", req.getRequestURI(), ex);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR, req.getRequestURI(), null);
  }

  private ResponseEntity<ApiError> build(HttpStatus status, String message, String path, Map<String, Object> details) {
    return ResponseEntity.status(status).body(
        new ApiError(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            details
        )
    );
  }
}
