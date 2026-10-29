package br.dev.lucasaguiar.mecano.inventory.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface SpringDataPartRepository extends JpaRepository<PartEntity, UUID> {

	Optional<PartEntity> findByCode(String code);

	boolean existsByCode(String code);

	List<PartEntity> findByActiveTrueOrderByName();

	List<PartEntity> findAllByOrderByName();

	@Query("SELECT p FROM PartEntity p WHERE p.active = TRUE AND p.availableQuantity <= p.minimumStock ORDER BY p.name")
	List<PartEntity> findAtOrBelowMinimumStock();
}
