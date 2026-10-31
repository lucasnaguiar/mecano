package br.dev.lucasaguiar.mecano.workorder.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Serviço do catálogo a incluir na OS")
public record AddLaborRequest(
		@Schema(description = "ID do serviço no catálogo") @NotNull(message = "O serviço é obrigatório.")
		UUID laborOperationId,
		@Schema(description = "Quantidade (padrão 1)", example = "1") @Min(value = 1, message = "A quantidade deve ser maior que zero.")
		Integer quantity) {

	public int quantityOrDefault() {
		return quantity == null ? 1 : quantity;
	}
}
