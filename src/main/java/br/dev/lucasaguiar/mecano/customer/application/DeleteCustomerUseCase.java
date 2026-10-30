package br.dev.lucasaguiar.mecano.customer.application;

import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerRepository;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleRepository;
import br.dev.lucasaguiar.mecano.shared.application.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Remove um cliente sem veículos cadastrados.
 */
@Service
public class DeleteCustomerUseCase {

	private final CustomerRepository customerRepository;
	private final VehicleRepository vehicleRepository;
	private final GetCustomerUseCase getCustomerUseCase;

	public DeleteCustomerUseCase(CustomerRepository customerRepository, VehicleRepository vehicleRepository,
			GetCustomerUseCase getCustomerUseCase) {
		this.customerRepository = customerRepository;
		this.vehicleRepository = vehicleRepository;
		this.getCustomerUseCase = getCustomerUseCase;
	}

	@Transactional
	public void execute(CustomerId id) {
		getCustomerUseCase.execute(id);
		if (!vehicleRepository.findByCustomerId(id).isEmpty()) {
			throw new ConflictException("O cliente possui veículos cadastrados e não pode ser removido.");
		}
		customerRepository.delete(id);
	}
}
