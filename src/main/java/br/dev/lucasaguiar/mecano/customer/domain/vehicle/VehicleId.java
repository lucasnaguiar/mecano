package br.dev.lucasaguiar.mecano.customer.domain.vehicle;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de Vehicle.
 */
public record VehicleId(UUID value) {

	public VehicleId {
		Objects.requireNonNull(value, "O identificador é obrigatório.");
	}

	public static VehicleId newId() {
		return new VehicleId(UUID.randomUUID());
	}
}
