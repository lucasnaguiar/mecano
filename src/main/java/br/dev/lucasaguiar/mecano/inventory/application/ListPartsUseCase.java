package br.dev.lucasaguiar.mecano.inventory.application;

import br.dev.lucasaguiar.mecano.inventory.domain.Part;
import br.dev.lucasaguiar.mecano.inventory.domain.PartRepository;
import br.dev.lucasaguiar.mecano.shared.domain.Page;
import br.dev.lucasaguiar.mecano.shared.domain.PageQuery;
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
	public Page<Part> execute(boolean includeInactive, boolean atOrBelowMinimumOnly, PageQuery query) {
		return atOrBelowMinimumOnly
				? partRepository.findAtOrBelowMinimumStock(query)
				: partRepository.findAll(includeInactive, query);
	}
}
