package br.dev.lucasaguiar.mecano.inventory.application;

import br.dev.lucasaguiar.mecano.inventory.domain.Part;
import br.dev.lucasaguiar.mecano.inventory.domain.PartRepository;
import br.dev.lucasaguiar.mecano.shared.application.ConflictException;
import br.dev.lucasaguiar.mecano.shared.application.DomainEventPublisher;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastra uma peça ou insumo com estoque zerado. O código é único.
 */
@Service
public class RegisterPartUseCase {

	private final PartRepository partRepository;
	private final DomainEventPublisher eventPublisher;

	public RegisterPartUseCase(PartRepository partRepository, DomainEventPublisher eventPublisher) {
		this.partRepository = partRepository;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public Part execute(RegisterPartCommand command) {
		var part = Part.register(command.code(), command.name(), command.type(), command.unitOfMeasure(),
				new Money(command.unitPrice()), command.minimumStock(), Instant.now());
		if (partRepository.findByCode(part.getCode()).isPresent()) {
			throw new ConflictException("Já existe um item com o código " + part.getCode() + ".");
		}
		var saved = partRepository.save(part);
		eventPublisher.publish(part.pullEvents());
		return saved;
	}
}
