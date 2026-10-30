package br.dev.lucasaguiar.mecano.inventory.application;

import br.dev.lucasaguiar.mecano.inventory.domain.Part;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.inventory.domain.PartRepository;
import br.dev.lucasaguiar.mecano.inventory.domain.StockMovement;
import br.dev.lucasaguiar.mecano.inventory.domain.StockMovementRepository;
import br.dev.lucasaguiar.mecano.shared.application.DomainEventPublisher;
import br.dev.lucasaguiar.mecano.shared.application.NotFoundException;
import br.dev.lucasaguiar.mecano.shared.domain.Quantity;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reserva e baixa as peças de uma ordem de serviço. Chamado pelas políticas que reagem aos eventos da OS, na mesma
 * transação: se faltar estoque, a operação da OS também é desfeita.
 */
@Service
public class WorkOrderStockUseCase {

	private final PartRepository partRepository;
	private final StockMovementRepository stockMovementRepository;
	private final DomainEventPublisher eventPublisher;

	public WorkOrderStockUseCase(PartRepository partRepository, StockMovementRepository stockMovementRepository,
			DomainEventPublisher eventPublisher) {
		this.partRepository = partRepository;
		this.stockMovementRepository = stockMovementRepository;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public void reserve(UUID workOrderId, Map<PartId, Quantity> quantities) {
		apply(quantities, (part, quantity, now) -> part.reserve(quantity, workOrderId, now));
	}

	@Transactional
	public void decrease(UUID workOrderId, Map<PartId, Quantity> quantities) {
		apply(quantities, (part, quantity, now) -> part.decrease(quantity, workOrderId, now));
	}

	private void apply(Map<PartId, Quantity> quantities, StockOperation operation) {
		var now = Instant.now();
		quantities.forEach((partId, quantity) -> {
			var part = partRepository.findById(partId)
					.orElseThrow(() -> new NotFoundException("Peça ou insumo não encontrado: " + partId.value()));
			var movement = operation.apply(part, quantity, now);
			partRepository.save(part);
			stockMovementRepository.save(movement);
			eventPublisher.publish(part.pullEvents());
		});
	}

	@FunctionalInterface
	private interface StockOperation {
		StockMovement apply(Part part, Quantity quantity, Instant now);
	}
}
