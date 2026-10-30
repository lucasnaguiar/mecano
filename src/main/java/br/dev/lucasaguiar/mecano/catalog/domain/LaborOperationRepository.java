package br.dev.lucasaguiar.mecano.catalog.domain;

import java.util.List;
import java.util.Optional;

/**
 * Repositório do agregado LaborOperation.
 */
public interface LaborOperationRepository {

	LaborOperation save(LaborOperation laborOperation);

	Optional<LaborOperation> findById(LaborOperationId id);

	Optional<LaborOperation> findByName(String name);

	List<LaborOperation> findAll(boolean includeInactive);
}
