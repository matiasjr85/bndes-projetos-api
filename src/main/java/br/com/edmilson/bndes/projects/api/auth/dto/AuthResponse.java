package br.com.edmilson.bndes.projects.api.auth.dto;

public record AuthResponse(
    String token,
    String tokenType,
    long expiresInSeconds,
    String refreshToken,
    long refreshExpiresInSeconds
) {}
