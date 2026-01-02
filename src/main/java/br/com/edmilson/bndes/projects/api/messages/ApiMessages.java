package br.com.edmilson.bndes.projects.api.messages;

public final class ApiMessages {

  private ApiMessages() {}

  public static final String UNEXPECTED_ERROR = "Unexpected error.";
  public static final String VALIDATION_ERROR = "Validation error.";
  public static final String INVALID_JSON_BODY = "Invalid JSON body.";
  public static final String PASSWORD_WEAK = "Password must be at least 8 characters and contain uppercase, lowercase, number, and special character.";
  public static final String UNAUTHORIZED = "Unauthorized.";
  public static final String ACCESS_DENIED = "Access denied.";
  public static final String INVALID_CREDENTIALS = "Invalid credentials.";
  public static final String USER_DISABLED = "User is disabled.";
  public static final String USER_ALREADY_EXISTS = "User already exists.";

  public static final String PROJECT_NOT_FOUND = "Project not found.";
  public static final String PROJECT_ACCESS_FORBIDDEN = "You do not have permission to access this project.";

  public static final String RESOURCE_ALREADY_EXISTS = "Resource already exists.";

  public static final String TOKEN_EXPIRED = "Token expired.";
  public static final String INVALID_TOKEN = "Invalid token.";
  public static final String TOKEN_REVOKED = "Token revoked.";
  public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token.";
}
