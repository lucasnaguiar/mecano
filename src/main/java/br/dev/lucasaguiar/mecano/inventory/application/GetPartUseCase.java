package br.dev.lucasaguiar.mecano.inventory.application;

import br.dev.lucasaguiar.mecano.inventory.domain.Part;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.inventory.domain.PartRepository;
import br.dev.lucasaguiar.mecano.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta uma peça ou insumo pelo ID.
 */
@Service
public class GetPartUseCase {

	private final PartRepository partRepository;

	public GetPartUseCase(PartRepository partRepository) {
		this.partRepository = partRepository;
	}

	@Transactional(readOnly = true)
	public Part execute(PartId id) {
		return partRepository.findById(id).orElseThrow(() -> new NotFoundException("Peça ou insumo não encontrado."));
	}
}
