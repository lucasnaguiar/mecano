package br.dev.lucasaguiar.mecano.catalog.presentation.dto;

import br.dev.lucasaguiar.mecano.catalog.application.LaborOperationData;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Dados de um serviço do catálogo")
public record LaborOperationRequest(
		@Schema(description = "Nome do serviço", example = "Troca de óleo")
		@NotBlank(message = "O nome é obrigatório.")
		String name,

		@Schema(description = "Descrição do que é feito")
		String description,

		@Schema(description = "Preço em reais", example = "120.00")
		@NotNull(message = "O preço é obrigatório.")
		BigDecimal price,

		@Schema(description = "Tempo estimado em minutos", example = "60")
		@NotNull(message = "O tempo estimado é obrigatório.") @Min(value = 1, message = "O tempo estimado deve ser de pelo menos 1 minuto.")
		Integer estimatedDurationMinutes) {

	public LaborOperationData toData() {
		return new LaborOperationData(name, description, price, estimatedDurationMinutes);
	}
}
