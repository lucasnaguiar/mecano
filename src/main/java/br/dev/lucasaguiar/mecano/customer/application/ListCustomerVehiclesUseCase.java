package br.dev.lucasaguiar.mecano.customer.application;

import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.Vehicle;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista os veículos de um cliente.
 */
@Service
public class ListCustomerVehiclesUseCase {

	private final VehicleRepository vehicleRepository;
	private final GetCustomerUseCase getCustomerUseCase;

	public ListCustomerVehiclesUseCase(VehicleRepository vehicleRepository, GetCustomerUseCase getCustomerUseCase) {
		this.vehicleRepository = vehicleRepository;
		this.getCustomerUseCase = getCustomerUseCase;
	}

	@Transactional(readOnly = true)
	public List<Vehicle> execute(CustomerId customerId) {
		getCustomerUseCase.execute(customerId);
		return vehicleRepository.findByCustomerId(customerId);
	}
}
