package br.dev.lucasaguiar.mecano.workorder.domain.event;

import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.time.Instant;

/**
 * Evento de domínio: diagnóstico iniciado.
 */
public record DiagnosisStarted(WorkOrderId workOrderId, Instant occurredAt) implements DomainEvent {
}
