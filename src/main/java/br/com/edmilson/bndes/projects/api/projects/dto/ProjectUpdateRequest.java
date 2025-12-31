package br.com.edmilson.bndes.projects.api.projects.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "ProjectUpdateRequest", description = "Payload para atualização (parcial) de um projeto existente")
public record ProjectUpdateRequest(

    @Schema(example = "Projeto de Modernização (Atualizado)")
    @Size(max = 120, message = "Name must have at most 120 characters.")
    String name,

    @Schema(example = "Descrição atualizada do projeto")
    @Size(max = 500, message = "Description must have at most 500 characters.")
    String description,

    @Schema(example = "200000.00")
    @DecimalMin(value = "0.00", inclusive = true, message = "Value must be >= 0.")
    BigDecimal value,

    @Schema(example = "false")
    Boolean active,

    @Schema(example = "2025-02-01")
    LocalDate startDate,

    @Schema(example = "2026-01-31")
    LocalDate endDate
) {}
