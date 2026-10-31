package br.dev.lucasaguiar.mecano.workorder.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;

@Schema(description = "Tempo médio de execução das OS finalizadas (de 'Em execução' a 'Finalizada')")
public record AverageExecutionTimeResponse(
		@Schema(description = "Média em segundos; nulo se nenhuma OS foi finalizada", example = "10800") Long averageSeconds,
		@Schema(description = "Média no formato ISO-8601", example = "PT3H") String averageDuration) {

	public static AverageExecutionTimeResponse from(Duration average) {
		return new AverageExecutionTimeResponse(average.toSeconds(), average.toString());
	}

	public static AverageExecutionTimeResponse empty() {
		return new AverageExecutionTimeResponse(null, null);
	}
}
