package br.dev.lucasaguiar.mecano.workorder.domain.event;

import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.time.Instant;

/**
 * Evento de domínio: orçamento recusado pelo cliente.
 */
public record EstimateRejected(WorkOrderId workOrderId, Instant occurredAt) implements DomainEvent {
}
