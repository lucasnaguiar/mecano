package br.dev.lucasaguiar.mecano.catalog.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataLaborOperationRepository extends JpaRepository<LaborOperationEntity, UUID> {

	List<LaborOperationEntity> findByActiveTrueOrderByName();

	boolean existsByName(String name);

	Optional<LaborOperationEntity> findByName(String name);
}
