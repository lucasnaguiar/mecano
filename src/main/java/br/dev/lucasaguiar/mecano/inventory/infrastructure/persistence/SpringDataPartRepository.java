package br.dev.lucasaguiar.mecano.inventory.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface SpringDataPartRepository extends JpaRepository<PartEntity, UUID> {

	Optional<PartEntity> findByCode(String code);

	boolean existsByCode(String code);

	Page<PartEntity> findByActiveTrue(Pageable pageable);

	@Query("SELECT p FROM PartEntity p WHERE p.active = TRUE AND p.availableQuantity <= p.minimumStock")
	Page<PartEntity> findAtOrBelowMinimumStock(Pageable pageable);
}
