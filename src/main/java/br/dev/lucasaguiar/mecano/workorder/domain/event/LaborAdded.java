package br.dev.lucasaguiar.mecano.workorder.domain.event;

import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.time.Instant;

/**
 * Evento de domínio: serviço do catálogo incluído na ordem de serviço.
 */
public record LaborAdded(WorkOrderId workOrderId, Instant occurredAt) implements DomainEvent {
}
