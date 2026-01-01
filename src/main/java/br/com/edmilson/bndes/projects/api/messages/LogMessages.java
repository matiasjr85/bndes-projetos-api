package br.com.edmilson.bndes.projects.api.messages;

public final class LogMessages {

  private LogMessages() {}

  public static final String AUTH_LOGIN_SUCCESS = "Success - User authenticated.";
  public static final String AUTH_REGISTER_SUCCESS = "Success - User registered.";

  public static final String PROJECT_CREATED = "Success - Project created.";
  public static final String PROJECT_UPDATED = "Success - Project updated.";
  public static final String PROJECT_DELETED = "Success - Project deleted.";
  public static final String PROJECT_LISTED = "Success - Projects listed.";

  public static final String VALIDATION_FAILED = "Error - Validation failed.";
  public static final String ACCESS_FORBIDDEN = "Error - Access forbidden.";
  public static final String UNAUTHORIZED_ACCESS = "Error - Unauthorized access.";
}
