package br.dev.lucasaguiar.mecano.inventory.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataStockMovementRepository extends JpaRepository<StockMovementEntity, UUID> {

	List<StockMovementEntity> findByPartIdOrderByOccurredAtDesc(UUID partId);

	List<StockMovementEntity> findByWorkOrderIdOrderByOccurredAt(UUID workOrderId);
}
