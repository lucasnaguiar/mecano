package br.dev.lucasaguiar.mecano.customer.presentation.dto;

import br.dev.lucasaguiar.mecano.customer.application.VehicleData;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados editáveis do veículo (placa e dono não mudam)")
public record UpdateVehicleRequest(
		@NotBlank(message = "A marca é obrigatória.") String brand,
		@NotBlank(message = "O modelo é obrigatório.") String model,
		@NotNull(message = "O ano do modelo é obrigatório.") Integer modelYear) {

	public VehicleData toData() {
		return new VehicleData(brand, model, modelYear);
	}
}
