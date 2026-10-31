package br.dev.lucasaguiar.mecano.auth.presentation.dto;

import br.dev.lucasaguiar.mecano.auth.domain.Role;
import br.dev.lucasaguiar.mecano.auth.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Usuário da equipe")
public record UserResponse(UUID id, String name, String email, Role role, boolean active, Instant createdAt,
		Instant updatedAt) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId().value(), user.getName(), user.getEmail(), user.getRole(), user.isActive(),
				user.getCreatedAt(), user.getUpdatedAt());
	}
}
