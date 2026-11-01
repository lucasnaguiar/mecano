package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cancela a OS a pedido da equipe, antes do início da execução. Se o orçamento já estava aprovado, a política de
 * estoque libera as peças reservadas na mesma transação.
 */
@Service
public class CancelWorkOrderUseCase {

	private final WorkOrderStore store;

	public CancelWorkOrderUseCase(WorkOrderStore store) {
		this.store = store;
	}

	@Transactional
	public WorkOrder execute(WorkOrderId id, String reason, UserId userId) {
		var workOrder = store.load(id);
		workOrder.cancel(reason, userId, Instant.now());
		return store.save(workOrder);
	}
}
