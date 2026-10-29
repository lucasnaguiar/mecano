package br.dev.lucasaguiar.mecano.customer.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataVehicleRepository extends JpaRepository<VehicleEntity, UUID> {

	Optional<VehicleEntity> findByLicensePlate(String licensePlate);

	boolean existsByLicensePlate(String licensePlate);

	List<VehicleEntity> findByCustomerId(UUID customerId);
}
