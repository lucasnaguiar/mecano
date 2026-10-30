package br.dev.lucasaguiar.mecano.inventory.presentation.dto;

import br.dev.lucasaguiar.mecano.inventory.application.RegisterPartCommand;
import br.dev.lucasaguiar.mecano.inventory.domain.PartType;
import br.dev.lucasaguiar.mecano.shared.domain.UnitOfMeasure;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Dados para cadastrar uma peça ou insumo (o estoque começa zerado)")
public record CreatePartRequest(
		@Schema(description = "Código interno (SKU)", example = "OLEO-5W30")
		@NotBlank(message = "O código é obrigatório.")
		String code,

		@Schema(description = "Nome", example = "Óleo de motor 5W30")
		@NotBlank(message = "O nome é obrigatório.")
		String name,

		@Schema(description = "PART (peça, por unidade) ou SUPPLY (insumo, aceita fração)")
		@NotNull(message = "O tipo é obrigatório.")
		PartType type,

		@Schema(description = "Unidade de medida: UN, L, ML, KG, G ou M")
		@NotNull(message = "A unidade de medida é obrigatória.")
		UnitOfMeasure unitOfMeasure,

		@Schema(description = "Preço de venda por unidade de medida", example = "45.90")
		@NotNull(message = "O preço é obrigatório.")
		BigDecimal unitPrice,

		@Schema(description = "Estoque mínimo antes de alertar reposição", example = "5")
		@NotNull(message = "O estoque mínimo é obrigatório.")
		BigDecimal minimumStock) {

	public RegisterPartCommand toCommand() {
		return new RegisterPartCommand(code, name, type, unitOfMeasure, unitPrice, minimumStock);
	}
}
