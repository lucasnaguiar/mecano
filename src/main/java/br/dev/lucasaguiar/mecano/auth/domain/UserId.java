package br.dev.lucasaguiar.mecano.auth.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de User.
 */
public record UserId(UUID value) {

	public UserId {
		Objects.requireNonNull(value, "O identificador do usuário é obrigatório.");
	}

	public static UserId newId() {
		return new UserId(UUID.randomUUID());
	}
}
