package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.workorder.domain.TrackingCode;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra a recusa do orçamento pelo cliente. A OS volta ao diagnóstico para ser reorçada.
 */
@Service
public class RejectEstimateUseCase {

	private final WorkOrderStore store;

	public RejectEstimateUseCase(WorkOrderStore store) {
		this.store = store;
	}

	@Transactional
	public WorkOrder execute(String trackingCode) {
		var workOrder = store.load(new TrackingCode(trackingCode));
		workOrder.rejectEstimate(Instant.now());
		return store.save(workOrder);
	}
}
