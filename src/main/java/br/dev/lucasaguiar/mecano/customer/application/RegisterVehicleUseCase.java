package br.dev.lucasaguiar.mecano.customer.application;

import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.Vehicle;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleRepository;
import br.dev.lucasaguiar.mecano.shared.application.ConflictException;
import br.dev.lucasaguiar.mecano.shared.application.DomainEventPublisher;
import br.dev.lucasaguiar.mecano.shared.domain.LicensePlate;
import br.dev.lucasaguiar.mecano.shared.domain.ModelYear;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastra um veículo para um cliente existente. A placa é única.
 */
@Service
public class RegisterVehicleUseCase {

	private final VehicleRepository vehicleRepository;
	private final GetCustomerUseCase getCustomerUseCase;
	private final DomainEventPublisher eventPublisher;

	public RegisterVehicleUseCase(VehicleRepository vehicleRepository, GetCustomerUseCase getCustomerUseCase,
			DomainEventPublisher eventPublisher) {
		this.vehicleRepository = vehicleRepository;
		this.getCustomerUseCase = getCustomerUseCase;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public Vehicle execute(CustomerId customerId, String licensePlate, VehicleData data) {
		getCustomerUseCase.execute(customerId);
		var plate = new LicensePlate(licensePlate);
		if (vehicleRepository.existsByLicensePlate(plate)) {
			throw new ConflictException("Já existe um veículo com a placa " + plate.value() + ".");
		}

		var vehicle = Vehicle.register(customerId, plate, data.brand(), data.model(), new ModelYear(data.modelYear()),
				Instant.now());
		var saved = vehicleRepository.save(vehicle);
		eventPublisher.publish(vehicle.pullEvents());
		return saved;
	}
}
