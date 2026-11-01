package br.dev.lucasaguiar.mecano.workorder.domain.event;

import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.time.Instant;
import java.util.List;

/**
 * Evento de domínio: OS cancelada pela equipe. Leva as peças que estavam reservadas (orçamento já aprovado) para a
 * reserva ser liberada; a lista é vazia quando nada havia sido reservado.
 */
public record WorkOrderCanceled(WorkOrderId workOrderId, List<RequiredPart> reservedParts, Instant occurredAt)
		implements DomainEvent {

	public WorkOrderCanceled {
		reservedParts = List.copyOf(reservedParts);
	}
}
