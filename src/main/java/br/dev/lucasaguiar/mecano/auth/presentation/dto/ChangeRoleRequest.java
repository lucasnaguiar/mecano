package br.dev.lucasaguiar.mecano.auth.presentation.dto;

import br.dev.lucasaguiar.mecano.auth.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Novo perfil de acesso")
public record ChangeRoleRequest(
		@Schema(description = "ADMIN, ATTENDANT, MECHANIC ou STOCK_MANAGER", example = "MECHANIC")
		@NotNull(message = "O perfil é obrigatório.")
		Role role) {
}
