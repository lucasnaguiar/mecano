package br.dev.lucasaguiar.mecano.catalog.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de LaborOperation.
 */
public record LaborOperationId(UUID value) {

	public LaborOperationId {
		Objects.requireNonNull(value, "O identificador é obrigatório.");
	}

	public static LaborOperationId newId() {
		return new LaborOperationId(UUID.randomUUID());
	}
}
