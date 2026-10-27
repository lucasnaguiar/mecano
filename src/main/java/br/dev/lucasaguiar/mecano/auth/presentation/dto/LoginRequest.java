package br.dev.lucasaguiar.mecano.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de acesso da equipe")
public record LoginRequest(
		@Schema(description = "E-mail do usuário", example = "admin@mecano.local")
		@NotBlank(message = "O e-mail é obrigatório.") @Email(message = "E-mail inválido.")
		String email,

		@Schema(description = "Senha do usuário")
		@NotBlank(message = "A senha é obrigatória.")
		String password) {
}
