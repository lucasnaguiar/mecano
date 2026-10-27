package br.dev.lucasaguiar.mecano.auth.presentation.dto;

import br.dev.lucasaguiar.mecano.auth.application.RegisterUserCommand;
import br.dev.lucasaguiar.mecano.auth.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastrar um usuário da equipe")
public record RegisterUserRequest(
		@Schema(description = "Nome do usuário")
		@NotBlank(message = "O nome é obrigatório.") @Size(min = 2, max = 255, message = "O nome deve ter entre 2 e 255 caracteres.")
		String name,

		@Schema(description = "E-mail usado no login")
		@NotBlank(message = "O e-mail é obrigatório.") @Email(message = "E-mail inválido.")
		String email,

		@Schema(description = "Senha com no mínimo 8 caracteres")
		@NotBlank(message = "A senha é obrigatória.") @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
		String password,

		@Schema(description = "Perfil de acesso")
		@NotNull(message = "O perfil é obrigatório.")
		Role role) {

	public RegisterUserCommand toCommand() {
		return new RegisterUserCommand(name, email, password, role);
	}
}
