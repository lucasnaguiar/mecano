package br.dev.lucasaguiar.mecano.inventory.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Dados editáveis da peça ou insumo")
public record UpdatePartRequest(
		@NotBlank(message = "O nome é obrigatório.") String name,
		@NotNull(message = "O estoque mínimo é obrigatório.") BigDecimal minimumStock) {
}
