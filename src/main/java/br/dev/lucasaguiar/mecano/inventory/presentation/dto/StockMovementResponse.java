package br.dev.lucasaguiar.mecano.inventory.presentation.dto;

import br.dev.lucasaguiar.mecano.inventory.domain.StockMovement;
import br.dev.lucasaguiar.mecano.inventory.domain.StockMovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Movimentação do extrato de estoque")
public record StockMovementResponse(UUID id, StockMovementType type, BigDecimal quantity, UUID workOrderId,
		UUID userId, Instant occurredAt) {

	public static StockMovementResponse from(StockMovement movement) {
		return new StockMovementResponse(movement.getId(), movement.getType(), movement.getQuantity().value(),
				movement.getWorkOrderId(), movement.getUserId() == null ? null : movement.getUserId().value(),
				movement.getOccurredAt());
	}
}
