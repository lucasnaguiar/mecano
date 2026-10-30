package br.dev.lucasaguiar.mecano.customer.application;

import br.dev.lucasaguiar.mecano.customer.domain.vehicle.Vehicle;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleRepository;
import br.dev.lucasaguiar.mecano.shared.domain.ModelYear;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Altera marca, modelo e ano do veículo. Placa e dono não mudam.
 */
@Service
public class UpdateVehicleUseCase {

	private final VehicleRepository vehicleRepository;
	private final GetVehicleUseCase getVehicleUseCase;

	public UpdateVehicleUseCase(VehicleRepository vehicleRepository, GetVehicleUseCase getVehicleUseCase) {
		this.vehicleRepository = vehicleRepository;
		this.getVehicleUseCase = getVehicleUseCase;
	}

	@Transactional
	public Vehicle execute(VehicleId id, VehicleData data) {
		var vehicle = getVehicleUseCase.execute(id);
		vehicle.update(data.brand(), data.model(), new ModelYear(data.modelYear()), Instant.now());
		return vehicleRepository.save(vehicle);
	}
}
