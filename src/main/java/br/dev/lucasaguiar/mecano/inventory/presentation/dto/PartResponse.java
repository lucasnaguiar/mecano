package br.dev.lucasaguiar.mecano.inventory.presentation.dto;

import br.dev.lucasaguiar.mecano.inventory.domain.Part;
import br.dev.lucasaguiar.mecano.inventory.domain.PartType;
import br.dev.lucasaguiar.mecano.shared.domain.UnitOfMeasure;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Peça ou insumo com saldos de estoque")
public record PartResponse(UUID id, String code, String name, PartType type, UnitOfMeasure unitOfMeasure,
		BigDecimal unitPrice, BigDecimal availableQuantity, BigDecimal reservedQuantity, BigDecimal minimumStock,
		boolean atOrBelowMinimumStock, boolean active) {

	public static PartResponse from(Part part) {
		return new PartResponse(part.getId().value(), part.getCode(), part.getName(), part.getType(),
				part.getUnitOfMeasure(), part.getUnitPrice().amount(), part.getAvailableQuantity(),
				part.getReservedQuantity(), part.getMinimumStock(), part.isAtOrBelowMinimumStock(), part.isActive());
	}
}
