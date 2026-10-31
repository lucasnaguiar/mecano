package br.dev.lucasaguiar.mecano.workorder.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Envio do orçamento ao cliente")
public record SendEstimateRequest(
		@Schema(description = "Observações do diagnóstico", example = "Pastilhas dianteiras gastas") String diagnosisNotes) {
}
