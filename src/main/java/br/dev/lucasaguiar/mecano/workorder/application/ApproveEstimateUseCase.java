package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.workorder.domain.TrackingCode;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra a aprovação do orçamento pelo cliente. Reserva as peças no estoque na mesma transação: se faltar estoque, a aprovação é recusada.
 */
@Service
public class ApproveEstimateUseCase {

	private final WorkOrderStore store;

	public ApproveEstimateUseCase(WorkOrderStore store) {
		this.store = store;
	}

	@Transactional
	public WorkOrder execute(String trackingCode) {
		var workOrder = store.load(new TrackingCode(trackingCode));
		workOrder.approveEstimate(Instant.now());
		return store.save(workOrder);
	}
}
