package br.com.edmilson.bndes.projects.api.projects.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "ProjectCreateRequest", description = "Payload para criação de um novo projeto")
public record ProjectCreateRequest(

    @Schema(example = "Projeto de Modernização", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required.")
    @Size(max = 120, message = "Name must have at most 120 characters.")
    String name,

    @Schema(example = "Projeto para modernizar sistemas legados", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Description is required.")
    @Size(max = 500, message = "Description must have at most 500 characters.")
    String description,

    @Schema(example = "150000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Value is required.")
    @DecimalMin(value = "0.00", inclusive = true, message = "Value must be >= 0.")
    BigDecimal value,

    @Schema(example = "true", description = "Se não informado, será true")
    Boolean active,

    @Schema(example = "2025-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Start date is required.")
    LocalDate startDate,

    @Schema(example = "2025-12-31")
    LocalDate endDate
) {}
