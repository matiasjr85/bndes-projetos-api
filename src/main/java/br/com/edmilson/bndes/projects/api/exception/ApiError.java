package br.com.edmilson.bndes.projects.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(

    @Schema(example = "2026-01-02T11:28:09.826Z")
    Instant timestamp,

    @Schema(example = "401")
    int status,

    @Schema(example = "Unauthorized")
    String error,

    @Schema(example = "Unauthorized.")
    String message,

    @Schema(example = "/projects")
    String path,

    @Schema(description = "Detalhes adicionais do erro (ex.: validações). Pode ser null.")
    Map<String, Object> details
) {}
