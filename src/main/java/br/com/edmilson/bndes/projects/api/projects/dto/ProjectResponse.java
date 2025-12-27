package br.com.edmilson.bndes.projects.api.projects.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ProjectResponse(
    Long id,
    String name,
    String description,
    BigDecimal value,
    Boolean active,
    LocalDate startDate,
    LocalDate endDate,
    Instant createdAt,
    Instant updatedAt
) {}
