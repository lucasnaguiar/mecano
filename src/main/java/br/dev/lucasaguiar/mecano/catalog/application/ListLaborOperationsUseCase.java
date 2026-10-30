package br.dev.lucasaguiar.mecano.catalog.application;

import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperation;
import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista os serviços do catálogo em ordem alfabética.
 */
@Service
public class ListLaborOperationsUseCase {

	private final LaborOperationRepository laborOperationRepository;

	public ListLaborOperationsUseCase(LaborOperationRepository laborOperationRepository) {
		this.laborOperationRepository = laborOperationRepository;
	}

	@Transactional(readOnly = true)
	public List<LaborOperation> execute(boolean includeInactive) {
		return laborOperationRepository.findAll(includeInactive);
	}
}
