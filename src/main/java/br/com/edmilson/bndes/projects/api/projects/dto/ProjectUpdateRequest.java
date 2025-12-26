package br.com.edmilson.bndes.projects.api.projects.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectUpdateRequest(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Size(max = 2000) String description,
    @NotNull BigDecimal value,
    @NotNull Boolean status,
    LocalDate startDate,
    LocalDate endDate
) {}
