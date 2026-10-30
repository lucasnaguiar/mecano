package br.dev.lucasaguiar.mecano.inventory.application;

import br.dev.lucasaguiar.mecano.inventory.domain.Part;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.inventory.domain.PartRepository;
import br.dev.lucasaguiar.mecano.shared.application.DomainEventPublisher;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Altera o preço de venda. Não afeta ordens de serviço já orçadas, que guardam uma cópia do preço.
 */
@Service
public class ChangePartPriceUseCase {

	private final PartRepository partRepository;
	private final GetPartUseCase getPartUseCase;
	private final DomainEventPublisher eventPublisher;

	public ChangePartPriceUseCase(PartRepository partRepository, GetPartUseCase getPartUseCase,
			DomainEventPublisher eventPublisher) {
		this.partRepository = partRepository;
		this.getPartUseCase = getPartUseCase;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public Part execute(PartId id, BigDecimal unitPrice) {
		var part = getPartUseCase.execute(id);
		part.changePrice(new Money(unitPrice), Instant.now());
		var saved = partRepository.save(part);
		eventPublisher.publish(part.pullEvents());
		return saved;
	}
}
