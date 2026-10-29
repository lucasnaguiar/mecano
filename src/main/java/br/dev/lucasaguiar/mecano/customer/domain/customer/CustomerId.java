package br.dev.lucasaguiar.mecano.customer.domain.customer;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de Customer.
 */
public record CustomerId(UUID value) {

	public CustomerId {
		Objects.requireNonNull(value, "O identificador é obrigatório.");
	}

	public static CustomerId newId() {
		return new CustomerId(UUID.randomUUID());
	}
}
