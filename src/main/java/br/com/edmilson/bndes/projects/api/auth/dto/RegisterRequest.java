package br.com.edmilson.bndes.projects.api.auth.dto;

import br.com.edmilson.bndes.projects.api.messages.ApiMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterRequest", description = "Payload para registro de usuário")
public record RegisterRequest(

    @Schema(example = "user@bndes.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be valid.")
    String email,

    @Schema(example = "Aa@12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
    @Pattern(
        regexp = "^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,72}$",
        message = ApiMessages.PASSWORD_WEAK
    )
    String password

) {}
