package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicia a execução do serviço após a aprovação do orçamento. Dispara a baixa das peças reservadas.
 */
@Service
public class StartExecutionUseCase {

	private final WorkOrderStore store;

	public StartExecutionUseCase(WorkOrderStore store) {
		this.store = store;
	}

	@Transactional
	public WorkOrder execute(WorkOrderId id, UserId userId) {
		var workOrder = store.load(id);
		workOrder.startExecution(userId, Instant.now());
		return store.save(workOrder);
	}
}
