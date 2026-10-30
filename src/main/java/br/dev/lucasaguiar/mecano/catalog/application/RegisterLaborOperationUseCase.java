package br.dev.lucasaguiar.mecano.catalog.application;

import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperation;
import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationRepository;
import br.dev.lucasaguiar.mecano.shared.application.ConflictException;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastra um serviço no catálogo. O nome é único.
 */
@Service
public class RegisterLaborOperationUseCase {

	private final LaborOperationRepository laborOperationRepository;

	public RegisterLaborOperationUseCase(LaborOperationRepository laborOperationRepository) {
		this.laborOperationRepository = laborOperationRepository;
	}

	@Transactional
	public LaborOperation execute(LaborOperationData data) {
		var operation = LaborOperation.register(data.name(), data.description(), new Money(data.price()),
				Duration.ofMinutes(data.estimatedDurationMinutes()), Instant.now());
		if (laborOperationRepository.findByName(operation.getName()).isPresent()) {
			throw new ConflictException("Já existe um serviço chamado \"" + operation.getName() + "\".");
		}
		return laborOperationRepository.save(operation);
	}
}
