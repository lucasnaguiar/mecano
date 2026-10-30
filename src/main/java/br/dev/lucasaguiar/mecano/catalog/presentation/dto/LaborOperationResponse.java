package br.dev.lucasaguiar.mecano.catalog.presentation.dto;

import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Serviço do catálogo")
public record LaborOperationResponse(UUID id, String name, String description, BigDecimal price,
		long estimatedDurationMinutes, boolean active) {

	public static LaborOperationResponse from(LaborOperation operation) {
		return new LaborOperationResponse(operation.getId().value(), operation.getName(), operation.getDescription(),
				operation.getPrice().amount(), operation.getEstimatedDuration().toMinutes(), operation.isActive());
	}
}
