package br.dev.lucasaguiar.mecano.catalog.application;

import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperation;
import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationId;
import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationRepository;
import br.dev.lucasaguiar.mecano.shared.application.ConflictException;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Altera nome, descrição, preço e tempo estimado de um serviço.
 */
@Service
public class UpdateLaborOperationUseCase {

	private final LaborOperationRepository laborOperationRepository;
	private final GetLaborOperationUseCase getLaborOperationUseCase;

	public UpdateLaborOperationUseCase(LaborOperationRepository laborOperationRepository,
			GetLaborOperationUseCase getLaborOperationUseCase) {
		this.laborOperationRepository = laborOperationRepository;
		this.getLaborOperationUseCase = getLaborOperationUseCase;
	}

	@Transactional
	public LaborOperation execute(LaborOperationId id, LaborOperationData data) {
		var operation = getLaborOperationUseCase.execute(id);
		operation.update(data.name(), data.description(), new Money(data.price()),
				Duration.ofMinutes(data.estimatedDurationMinutes()), Instant.now());
		laborOperationRepository.findByName(operation.getName())
				.filter(other -> !other.getId().equals(id))
				.ifPresent(other -> {
					throw new ConflictException("Já existe um serviço chamado \"" + operation.getName() + "\".");
				});
		return laborOperationRepository.save(operation);
	}
}
