package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Envia o orçamento para aprovação do cliente, registrando as observações do diagnóstico.
 */
@Service
public class SendEstimateUseCase {

	private final WorkOrderStore store;

	public SendEstimateUseCase(WorkOrderStore store) {
		this.store = store;
	}

	@Transactional
	public WorkOrder execute(WorkOrderId id, String diagnosisNotes, UserId sentBy) {
		var workOrder = store.load(id);
		workOrder.sendEstimate(diagnosisNotes, sentBy, Instant.now());
		return store.save(workOrder);
	}
}
