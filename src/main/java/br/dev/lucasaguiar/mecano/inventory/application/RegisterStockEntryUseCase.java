package br.dev.lucasaguiar.mecano.inventory.application;

import br.dev.lucasaguiar.mecano.inventory.domain.Part;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.inventory.domain.PartRepository;
import br.dev.lucasaguiar.mecano.inventory.domain.StockMovementRepository;
import br.dev.lucasaguiar.mecano.shared.application.DomainEventPublisher;
import br.dev.lucasaguiar.mecano.shared.domain.Quantity;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra o recebimento de novas unidades de uma peça ou insumo.
 */
@Service
public class RegisterStockEntryUseCase {

	private final PartRepository partRepository;
	private final StockMovementRepository stockMovementRepository;
	private final GetPartUseCase getPartUseCase;
	private final DomainEventPublisher eventPublisher;

	public RegisterStockEntryUseCase(PartRepository partRepository, StockMovementRepository stockMovementRepository,
			GetPartUseCase getPartUseCase, DomainEventPublisher eventPublisher) {
		this.partRepository = partRepository;
		this.stockMovementRepository = stockMovementRepository;
		this.getPartUseCase = getPartUseCase;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public Part execute(PartId id, BigDecimal quantity, UserId registeredBy) {
		var part = getPartUseCase.execute(id);
		var movement = part.registerEntry(new Quantity(quantity), registeredBy, Instant.now());
		var saved = partRepository.save(part);
		stockMovementRepository.save(movement);
		eventPublisher.publish(part.pullEvents());
		return saved;
	}
}
