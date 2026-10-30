package br.dev.lucasaguiar.mecano.inventory.domain;

import br.dev.lucasaguiar.mecano.shared.domain.Quantity;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Registro de uma alteração no estoque de uma peça ou insumo, como um extrato.
 * A ordem de serviço é guardada apenas como referência (UUID) para não acoplar o domínio de estoque ao da OS.
 */
public class StockMovement {

	private final UUID id;
	private final PartId partId;
	private final StockMovementType type;
	private final Quantity quantity;
	private final UUID workOrderId;
	private final UserId userId;
	private final Instant occurredAt;

	private StockMovement(UUID id, PartId partId, StockMovementType type, Quantity quantity, UUID workOrderId,
			UserId userId, Instant occurredAt) {
		this.id = Objects.requireNonNull(id);
		this.partId = Objects.requireNonNull(partId);
		this.type = Objects.requireNonNull(type);
		this.quantity = Objects.requireNonNull(quantity);
		this.workOrderId = workOrderId;
		this.userId = userId;
		this.occurredAt = Objects.requireNonNull(occurredAt);
	}

	static StockMovement record(PartId partId, StockMovementType type, Quantity quantity, UUID workOrderId,
			UserId userId, Instant occurredAt) {
		return new StockMovement(UUID.randomUUID(), partId, type, quantity, workOrderId, userId, occurredAt);
	}

	public static StockMovement restore(UUID id, PartId partId, StockMovementType type, Quantity quantity,
			UUID workOrderId, UserId userId, Instant occurredAt) {
		return new StockMovement(id, partId, type, quantity, workOrderId, userId, occurredAt);
	}

	public UUID getId() {
		return id;
	}

	public PartId getPartId() {
		return partId;
	}

	public StockMovementType getType() {
		return type;
	}

	public Quantity getQuantity() {
		return quantity;
	}

	public UUID getWorkOrderId() {
		return workOrderId;
	}

	public UserId getUserId() {
		return userId;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}
}
