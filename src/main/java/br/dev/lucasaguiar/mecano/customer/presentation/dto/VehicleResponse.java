package br.dev.lucasaguiar.mecano.customer.presentation.dto;

import br.dev.lucasaguiar.mecano.customer.domain.vehicle.Vehicle;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Veículo de um cliente")
public record VehicleResponse(UUID id, UUID customerId, String licensePlate, String brand, String model,
		int modelYear) {

	public static VehicleResponse from(Vehicle vehicle) {
		return new VehicleResponse(vehicle.getId().value(), vehicle.getCustomerId().value(),
				vehicle.getLicensePlate().value(), vehicle.getBrand(), vehicle.getModel(),
				vehicle.getModelYear().value());
	}
}
