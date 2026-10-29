package br.dev.lucasaguiar.mecano.inventory.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de Part.
 */
public record PartId(UUID value) {

	public PartId {
		Objects.requireNonNull(value, "O identificador é obrigatório.");
	}

	public static PartId newId() {
		return new PartId(UUID.randomUUID());
	}
}
