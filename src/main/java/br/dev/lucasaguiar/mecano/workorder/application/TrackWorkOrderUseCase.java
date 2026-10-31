package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.workorder.domain.TrackingCode;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta o andamento da OS pelo código de acompanhamento (acesso do cliente).
 */
@Service
public class TrackWorkOrderUseCase {

	private final WorkOrderStore store;

	public TrackWorkOrderUseCase(WorkOrderStore store) {
		this.store = store;
	}

	@Transactional(readOnly = true)
	public WorkOrder execute(String trackingCode) {
		return store.load(new TrackingCode(trackingCode));
	}
}
