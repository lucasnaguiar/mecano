package br.dev.lucasaguiar.mecano.customer.application;

import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Remove um veículo. Veículos com ordens de serviço são protegidos pela integridade do banco (409).
 */
@Service
public class DeleteVehicleUseCase {

	private final VehicleRepository vehicleRepository;
	private final GetVehicleUseCase getVehicleUseCase;

	public DeleteVehicleUseCase(VehicleRepository vehicleRepository, GetVehicleUseCase getVehicleUseCase) {
		this.vehicleRepository = vehicleRepository;
		this.getVehicleUseCase = getVehicleUseCase;
	}

	@Transactional
	public void execute(VehicleId id) {
		getVehicleUseCase.execute(id);
		vehicleRepository.delete(id);
	}
}
