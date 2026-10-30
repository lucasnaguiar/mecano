package br.dev.lucasaguiar.mecano.customer.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.Vehicle;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleRepository;
import br.dev.lucasaguiar.mecano.shared.domain.LicensePlate;
import br.dev.lucasaguiar.mecano.shared.domain.ModelYear;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Implementação JPA de VehicleRepository.
 */
@Repository
public class JpaVehicleRepository implements VehicleRepository {

	private final SpringDataVehicleRepository springDataRepository;

	public JpaVehicleRepository(SpringDataVehicleRepository springDataRepository) {
		this.springDataRepository = springDataRepository;
	}

	@Override
	public Vehicle save(Vehicle vehicle) {
		return toDomain(springDataRepository.save(toEntity(vehicle)));
	}

	@Override
	public Optional<Vehicle> findById(VehicleId id) {
		return springDataRepository.findById(id.value()).map(JpaVehicleRepository::toDomain);
	}

	@Override
	public Optional<Vehicle> findByLicensePlate(LicensePlate licensePlate) {
		return springDataRepository.findByLicensePlate(licensePlate.value()).map(JpaVehicleRepository::toDomain);
	}

	@Override
	public boolean existsByLicensePlate(LicensePlate licensePlate) {
		return springDataRepository.existsByLicensePlate(licensePlate.value());
	}

	@Override
	public List<Vehicle> findByCustomerId(CustomerId customerId) {
		return springDataRepository.findByCustomerId(customerId.value()).stream()
				.map(JpaVehicleRepository::toDomain).toList();
	}

	@Override
	public void delete(VehicleId id) {
		springDataRepository.deleteById(id.value());
		springDataRepository.flush();
	}

	private static VehicleEntity toEntity(Vehicle vehicle) {
		return new VehicleEntity(vehicle.getId().value(), vehicle.getCustomerId().value(),
				vehicle.getLicensePlate().value(), vehicle.getBrand(), vehicle.getModel(),
				(short) vehicle.getModelYear().value(), vehicle.getCreatedAt(), vehicle.getUpdatedAt());
	}

	private static Vehicle toDomain(VehicleEntity entity) {
		return Vehicle.restore(new VehicleId(entity.getId()), new CustomerId(entity.getCustomerId()),
				new LicensePlate(entity.getLicensePlate()), entity.getBrand(), entity.getModel(),
				new ModelYear(entity.getModelYear()), entity.getCreatedAt(), entity.getUpdatedAt());
	}
}
