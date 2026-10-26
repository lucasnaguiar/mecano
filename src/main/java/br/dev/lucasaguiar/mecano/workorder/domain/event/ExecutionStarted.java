package br.dev.lucasaguiar.mecano.workorder.domain.event;

import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.time.Instant;

/**
 * Evento de domínio: execução do serviço iniciada.
 */
public record ExecutionStarted(WorkOrderId workOrderId, Instant occurredAt) implements DomainEvent {
}
