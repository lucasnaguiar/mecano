package br.dev.lucasaguiar.mecano.catalog.application;

import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperation;
import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationId;
import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationRepository;
import br.dev.lucasaguiar.mecano.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta um serviço do catálogo pelo ID.
 */
@Service
public class GetLaborOperationUseCase {

	private final LaborOperationRepository laborOperationRepository;

	public GetLaborOperationUseCase(LaborOperationRepository laborOperationRepository) {
		this.laborOperationRepository = laborOperationRepository;
	}

	@Transactional(readOnly = true)
	public LaborOperation execute(LaborOperationId id) {
		return laborOperationRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Serviço não encontrado no catálogo."));
	}
}
