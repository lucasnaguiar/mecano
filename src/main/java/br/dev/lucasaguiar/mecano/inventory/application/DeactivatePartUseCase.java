package br.dev.lucasaguiar.mecano.inventory.application;

import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.inventory.domain.PartRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Desativa uma peça ou insumo: não pode mais ser incluído em OS, mas o histórico é mantido.
 */
@Service
public class DeactivatePartUseCase {

	private final PartRepository partRepository;
	private final GetPartUseCase getPartUseCase;

	public DeactivatePartUseCase(PartRepository partRepository, GetPartUseCase getPartUseCase) {
		this.partRepository = partRepository;
		this.getPartUseCase = getPartUseCase;
	}

	@Transactional
	public void execute(PartId id) {
		var part = getPartUseCase.execute(id);
		part.deactivate(Instant.now());
		partRepository.save(part);
	}
}
