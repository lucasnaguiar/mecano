package br.dev.lucasaguiar.mecano.inventory.application;

import br.dev.lucasaguiar.mecano.inventory.domain.Part;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.inventory.domain.PartRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Altera nome e estoque mínimo. Saldo e preço têm operações próprias.
 */
@Service
public class UpdatePartUseCase {

	private final PartRepository partRepository;
	private final GetPartUseCase getPartUseCase;

	public UpdatePartUseCase(PartRepository partRepository, GetPartUseCase getPartUseCase) {
		this.partRepository = partRepository;
		this.getPartUseCase = getPartUseCase;
	}

	@Transactional
	public Part execute(PartId id, String name, BigDecimal minimumStock) {
		var part = getPartUseCase.execute(id);
		part.update(name, minimumStock, Instant.now());
		return partRepository.save(part);
	}
}
