package br.dev.lucasaguiar.mecano.customer.domain.vehicle;

import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.shared.domain.LicensePlate;
import java.util.List;
import java.util.Optional;

/**
 * Repositório do agregado Vehicle.
 */
public interface VehicleRepository {

	Vehicle save(Vehicle vehicle);

	Optional<Vehicle> findById(VehicleId id);

	Optional<Vehicle> findByLicensePlate(LicensePlate licensePlate);

	boolean existsByLicensePlate(LicensePlate licensePlate);

	List<Vehicle> findByCustomerId(CustomerId customerId);

	void delete(VehicleId id);
}
