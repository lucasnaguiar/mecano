package br.dev.lucasaguiar.mecano.workorder.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Peça ou insumo a incluir na OS")
public record AddPartRequest(
		@Schema(description = "ID da peça ou insumo") @NotNull(message = "A peça é obrigatória.") UUID partId,
		@Schema(description = "Quantidade na unidade de medida do item", example = "4")
		@NotNull(message = "A quantidade é obrigatória.") BigDecimal quantity) {
}
