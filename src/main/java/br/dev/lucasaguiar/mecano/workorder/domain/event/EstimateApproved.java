package br.dev.lucasaguiar.mecano.workorder.domain.event;

import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.time.Instant;
import java.util.List;

/**
 * Evento de domínio: orçamento aprovado pelo cliente. Leva as peças a reservar no estoque.
 */
public record EstimateApproved(WorkOrderId workOrderId, List<RequiredPart> parts, Instant occurredAt)
		implements DomainEvent {

	public EstimateApproved {
		parts = List.copyOf(parts);
	}
}
