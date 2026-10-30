package br.dev.lucasaguiar.mecano.catalog.application;

import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationId;
import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Desativa um serviço: ele deixa de poder ser incluído em novas ordens de serviço, mas o histórico é mantido.
 */
@Service
public class DeactivateLaborOperationUseCase {

	private final LaborOperationRepository laborOperationRepository;
	private final GetLaborOperationUseCase getLaborOperationUseCase;

	public DeactivateLaborOperationUseCase(LaborOperationRepository laborOperationRepository,
			GetLaborOperationUseCase getLaborOperationUseCase) {
		this.laborOperationRepository = laborOperationRepository;
		this.getLaborOperationUseCase = getLaborOperationUseCase;
	}

	@Transactional
	public void execute(LaborOperationId id) {
		var operation = getLaborOperationUseCase.execute(id);
		operation.deactivate(Instant.now());
		laborOperationRepository.save(operation);
	}
}
