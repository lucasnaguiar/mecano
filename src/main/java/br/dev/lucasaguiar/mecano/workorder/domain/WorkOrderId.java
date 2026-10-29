package br.dev.lucasaguiar.mecano.workorder.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de WorkOrder.
 */
public record WorkOrderId(UUID value) {

	public WorkOrderId {
		Objects.requireNonNull(value, "O identificador é obrigatório.");
	}

	public static WorkOrderId newId() {
		return new WorkOrderId(UUID.randomUUID());
	}
}
