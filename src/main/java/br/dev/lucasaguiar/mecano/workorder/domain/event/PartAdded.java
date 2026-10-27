package br.dev.lucasaguiar.mecano.workorder.domain.event;

import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.time.Instant;

/**
 * Evento de domínio: peça ou insumo incluído na ordem de serviço.
 */
public record PartAdded(WorkOrderId workOrderId, Instant occurredAt) implements DomainEvent {
}
