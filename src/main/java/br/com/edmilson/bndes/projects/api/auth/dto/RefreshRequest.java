package br.com.edmilson.bndes.projects.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank(message = "refreshToken is required.")
    String refreshToken
) {}
