package br.dev.lucasaguiar.mecano.workorder.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Dados para abrir uma ordem de serviço")
public record OpenWorkOrderRequest(
		@Schema(description = "ID do cliente") @NotNull(message = "O cliente é obrigatório.") UUID customerId,
		@Schema(description = "ID do veículo do cliente") @NotNull(message = "O veículo é obrigatório.") UUID vehicleId,
		@Schema(description = "Relato do cliente sobre o problema", example = "Barulho ao frear")
		@NotBlank(message = "A descrição do problema é obrigatória.") String problemDescription) {
}
