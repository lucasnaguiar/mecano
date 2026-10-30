package br.dev.lucasaguiar.mecano.inventory.application;

import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.inventory.domain.StockMovement;
import br.dev.lucasaguiar.mecano.inventory.domain.StockMovementRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Extrato de movimentações de uma peça ou insumo, da mais recente para a mais antiga.
 */
@Service
public class ListStockMovementsUseCase {

	private final StockMovementRepository stockMovementRepository;
	private final GetPartUseCase getPartUseCase;

	public ListStockMovementsUseCase(StockMovementRepository stockMovementRepository, GetPartUseCase getPartUseCase) {
		this.stockMovementRepository = stockMovementRepository;
		this.getPartUseCase = getPartUseCase;
	}

	@Transactional(readOnly = true)
	public List<StockMovement> execute(PartId id) {
		getPartUseCase.execute(id);
		return stockMovementRepository.findByPartId(id);
	}
}
