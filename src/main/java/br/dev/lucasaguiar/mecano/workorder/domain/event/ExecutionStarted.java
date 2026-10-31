package br.dev.lucasaguiar.mecano.workorder.domain.event;

import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.time.Instant;
import java.util.List;

/**
 * Evento de domínio: execução do serviço iniciada. Leva as peças reservadas a baixar do estoque.
 */
public record ExecutionStarted(WorkOrderId workOrderId, List<RequiredPart> parts, Instant occurredAt)
		implements DomainEvent {

	public ExecutionStarted {
		parts = List.copyOf(parts);
	}
}
