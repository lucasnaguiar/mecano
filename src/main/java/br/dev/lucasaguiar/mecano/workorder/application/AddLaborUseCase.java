package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationId;
import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationRepository;
import br.dev.lucasaguiar.mecano.shared.application.NotFoundException;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inclui um serviço do catálogo na OS, copiando nome e preço atuais.
 */
@Service
public class AddLaborUseCase {

	private final WorkOrderStore store;
	private final LaborOperationRepository laborOperationRepository;

	public AddLaborUseCase(WorkOrderStore store, LaborOperationRepository laborOperationRepository) {
		this.store = store;
		this.laborOperationRepository = laborOperationRepository;
	}

	@Transactional
	public WorkOrder execute(WorkOrderId id, LaborOperationId laborOperationId, int quantity) {
		var workOrder = store.load(id);
		var operation = laborOperationRepository.findById(laborOperationId)
				.orElseThrow(() -> new NotFoundException("Serviço não encontrado no catálogo."));
		operation.ensureActive();
		workOrder.addLabor(operation.getId(), operation.getName(), operation.getPrice(), quantity, Instant.now());
		return store.save(workOrder);
	}
}
