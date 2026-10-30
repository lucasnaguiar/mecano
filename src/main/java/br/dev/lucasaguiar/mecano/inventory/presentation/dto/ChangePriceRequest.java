package br.dev.lucasaguiar.mecano.inventory.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Novo preço de venda")
public record ChangePriceRequest(
		@Schema(example = "49.90") @NotNull(message = "O preço é obrigatório.") BigDecimal unitPrice) {
}
