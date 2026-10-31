package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.inventory.domain.PartRepository;
import br.dev.lucasaguiar.mecano.shared.application.NotFoundException;
import br.dev.lucasaguiar.mecano.shared.domain.Quantity;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inclui uma peça ou insumo na OS, copiando nome, unidade e preço atuais. A disponibilidade é verificada na aprovação.
 */
@Service
public class AddPartUseCase {

	private final WorkOrderStore store;
	private final PartRepository partRepository;

	public AddPartUseCase(WorkOrderStore store, PartRepository partRepository) {
		this.store = store;
		this.partRepository = partRepository;
	}

	@Transactional
	public WorkOrder execute(WorkOrderId id, PartId partId, BigDecimal quantity) {
		var workOrder = store.load(id);
		var part = partRepository.findById(partId)
				.orElseThrow(() -> new NotFoundException("Peça ou insumo não encontrado."));
		part.ensureActive();
		workOrder.addPart(part.getId(), part.getName(), part.getUnitOfMeasure(), part.getUnitPrice(),
				new Quantity(quantity), Instant.now());
		return store.save(workOrder);
	}
}
