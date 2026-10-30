package br.dev.lucasaguiar.mecano.inventory.application;

import br.dev.lucasaguiar.mecano.inventory.domain.Part;
import br.dev.lucasaguiar.mecano.inventory.domain.PartRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista peças e insumos, opcionalmente só os que estão no estoque mínimo ou abaixo dele.
 */
@Service
public class ListPartsUseCase {

	private final PartRepository partRepository;

	public ListPartsUseCase(PartRepository partRepository) {
		this.partRepository = partRepository;
	}

	@Transactional(readOnly = true)
	public List<Part> execute(boolean includeInactive, boolean atOrBelowMinimumOnly) {
		return atOrBelowMinimumOnly ? partRepository.findAtOrBelowMinimumStock() : partRepository.findAll(includeInactive);
	}
}
