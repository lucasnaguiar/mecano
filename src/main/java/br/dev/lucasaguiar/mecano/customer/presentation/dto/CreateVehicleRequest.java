package br.dev.lucasaguiar.mecano.customer.presentation.dto;

import br.dev.lucasaguiar.mecano.customer.application.VehicleData;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Dados para cadastrar um veículo")
public record CreateVehicleRequest(
		@Schema(description = "ID do cliente dono do veículo")
		@NotNull(message = "O cliente é obrigatório.")
		UUID customerId,

		@Schema(description = "Placa no formato antigo ou Mercosul", example = "BRA2E19")
		@NotBlank(message = "A placa é obrigatória.")
		String licensePlate,

		@Schema(description = "Marca", example = "Fiat")
		@NotBlank(message = "A marca é obrigatória.")
		String brand,

		@Schema(description = "Modelo", example = "Uno")
		@NotBlank(message = "O modelo é obrigatório.")
		String model,

		@Schema(description = "Ano do modelo", example = "2015")
		@NotNull(message = "O ano do modelo é obrigatório.")
		Integer modelYear) {

	public VehicleData toData() {
		return new VehicleData(brand, model, modelYear);
	}
}
