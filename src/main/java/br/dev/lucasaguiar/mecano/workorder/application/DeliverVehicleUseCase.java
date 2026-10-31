package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra a entrega do veículo ao cliente.
 */
@Service
public class DeliverVehicleUseCase {

	private final WorkOrderStore store;

	public DeliverVehicleUseCase(WorkOrderStore store) {
		this.store = store;
	}

	@Transactional
	public WorkOrder execute(WorkOrderId id, UserId userId) {
		var workOrder = store.load(id);
		workOrder.deliver(userId, Instant.now());
		return store.save(workOrder);
	}
}
