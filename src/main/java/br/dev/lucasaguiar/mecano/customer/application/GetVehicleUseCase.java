package br.dev.lucasaguiar.mecano.customer.application;

import br.dev.lucasaguiar.mecano.customer.domain.vehicle.Vehicle;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleRepository;
import br.dev.lucasaguiar.mecano.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta um veículo pelo ID.
 */
@Service
public class GetVehicleUseCase {

	private final VehicleRepository vehicleRepository;

	public GetVehicleUseCase(VehicleRepository vehicleRepository) {
		this.vehicleRepository = vehicleRepository;
	}

	@Transactional(readOnly = true)
	public Vehicle execute(VehicleId id) {
		return vehicleRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Veículo não encontrado."));
	}
}
