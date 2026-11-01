package br.dev.lucasaguiar.mecano.workorder.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Cancelamento da ordem de serviço pela equipe")
public record CancelWorkOrderRequest(
		@Schema(description = "Motivo do cancelamento", example = "Cliente desistiu do serviço")
		@NotBlank(message = "O motivo do cancelamento é obrigatório.") String reason) {
}
