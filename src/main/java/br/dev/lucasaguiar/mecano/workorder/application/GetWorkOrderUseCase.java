package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta uma OS pelo ID (acesso da equipe).
 */
@Service
public class GetWorkOrderUseCase {

	private final WorkOrderStore store;

	public GetWorkOrderUseCase(WorkOrderStore store) {
		this.store = store;
	}

	@Transactional(readOnly = true)
	public WorkOrder execute(WorkOrderId id) {
		return store.load(id);
	}
}
