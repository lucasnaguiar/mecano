package br.dev.lucasaguiar.mecano.inventory.domain;

import java.util.List;

/**
 * Repositório do extrato de movimentações de estoque.
 */
public interface StockMovementRepository {

	void save(StockMovement movement);

	List<StockMovement> findByPartId(PartId partId);
}
