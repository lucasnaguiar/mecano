package br.dev.lucasaguiar.mecano.shared.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador do usuário da equipe. Fica em shared para que outros contextos o referenciem sem depender de auth.
 */
public record UserId(UUID value) {

	public UserId {
		Objects.requireNonNull(value, "O identificador do usuário é obrigatório.");
	}

	public static UserId newId() {
		return new UserId(UUID.randomUUID());
	}
}
