package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Remove um serviço da OS durante o diagnóstico.
 */
@Service
public class RemoveLaborItemUseCase {

	private final WorkOrderStore store;

	public RemoveLaborItemUseCase(WorkOrderStore store) {
		this.store = store;
	}

	@Transactional
	public WorkOrder execute(WorkOrderId id, UUID itemId) {
		var workOrder = store.load(id);
		workOrder.removeLabor(itemId, Instant.now());
		return store.save(workOrder);
	}
}
