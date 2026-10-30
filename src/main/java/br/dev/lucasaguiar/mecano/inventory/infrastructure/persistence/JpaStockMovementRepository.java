package br.dev.lucasaguiar.mecano.inventory.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.inventory.domain.StockMovement;
import br.dev.lucasaguiar.mecano.inventory.domain.StockMovementRepository;
import br.dev.lucasaguiar.mecano.shared.domain.Quantity;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Implementação JPA de StockMovementRepository.
 */
@Repository
public class JpaStockMovementRepository implements StockMovementRepository {

	private final SpringDataStockMovementRepository springDataRepository;

	public JpaStockMovementRepository(SpringDataStockMovementRepository springDataRepository) {
		this.springDataRepository = springDataRepository;
	}

	@Override
	public void save(StockMovement movement) {
		springDataRepository.save(new StockMovementEntity(movement.getId(), movement.getPartId().value(),
				movement.getWorkOrderId(), movement.getUserId() == null ? null : movement.getUserId().value(),
				movement.getType(), movement.getQuantity().value(), movement.getOccurredAt()));
	}

	@Override
	public List<StockMovement> findByPartId(PartId partId) {
		return springDataRepository.findByPartIdOrderByOccurredAtDesc(partId.value()).stream()
				.map(entity -> StockMovement.restore(entity.getId(), new PartId(entity.getPartId()), entity.getType(),
						new Quantity(entity.getQuantity()), entity.getWorkOrderId(),
						entity.getUserId() == null ? null : new UserId(entity.getUserId()), entity.getOccurredAt()))
				.toList();
	}
}
