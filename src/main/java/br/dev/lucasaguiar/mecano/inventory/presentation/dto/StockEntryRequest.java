package br.dev.lucasaguiar.mecano.inventory.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Recebimento de novas unidades no estoque")
public record StockEntryRequest(
		@Schema(description = "Quantidade recebida, na unidade de medida do item", example = "20")
		@NotNull(message = "A quantidade é obrigatória.")
		BigDecimal quantity) {
}
