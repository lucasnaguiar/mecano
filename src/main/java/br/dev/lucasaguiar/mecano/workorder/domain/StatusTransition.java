package br.dev.lucasaguiar.mecano.workorder.domain;

import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Registro de uma mudança de status da OS. changedBy nulo indica ação do cliente ou do sistema.
 */
public class StatusTransition {

	private final UUID id;
	private final WorkOrderStatus fromStatus;
	private final WorkOrderStatus toStatus;
	private final UserId changedBy;
	private final Instant occurredAt;

	private StatusTransition(UUID id, WorkOrderStatus fromStatus, WorkOrderStatus toStatus, UserId changedBy,
			Instant occurredAt) {
		this.id = Objects.requireNonNull(id);
		this.fromStatus = fromStatus;
		this.toStatus = Objects.requireNonNull(toStatus);
		this.changedBy = changedBy;
		this.occurredAt = Objects.requireNonNull(occurredAt);
	}

	static StatusTransition record(WorkOrderStatus fromStatus, WorkOrderStatus toStatus, UserId changedBy,
			Instant occurredAt) {
		return new StatusTransition(UUID.randomUUID(), fromStatus, toStatus, changedBy, occurredAt);
	}

	public static StatusTransition restore(UUID id, WorkOrderStatus fromStatus, WorkOrderStatus toStatus,
			UserId changedBy, Instant occurredAt) {
		return new StatusTransition(id, fromStatus, toStatus, changedBy, occurredAt);
	}

	public UUID getId() {
		return id;
	}

	public WorkOrderStatus getFromStatus() {
		return fromStatus;
	}

	public WorkOrderStatus getToStatus() {
		return toStatus;
	}

	public UserId getChangedBy() {
		return changedBy;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}
}
