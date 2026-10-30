package br.dev.lucasaguiar.mecano.customer.application;

import br.dev.lucasaguiar.mecano.customer.domain.vehicle.Vehicle;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleRepository;
import br.dev.lucasaguiar.mecano.shared.application.NotFoundException;
import br.dev.lucasaguiar.mecano.shared.domain.LicensePlate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Busca um veículo pela placa, como na recepção da oficina.
 */
@Service
public class FindVehicleByLicensePlateUseCase {

	private final VehicleRepository vehicleRepository;

	public FindVehicleByLicensePlateUseCase(VehicleRepository vehicleRepository) {
		this.vehicleRepository = vehicleRepository;
	}

	@Transactional(readOnly = true)
	public Vehicle execute(String licensePlate) {
		return vehicleRepository.findByLicensePlate(new LicensePlate(licensePlate))
				.orElseThrow(() -> new NotFoundException("Veículo não encontrado."));
	}
}
