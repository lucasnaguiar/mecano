package br.dev.lucasaguiar.mecano.inventory.domain;

import java.util.List;
import java.util.Optional;

/**
 * Repositório do agregado Part.
 */
public interface PartRepository {

	Part save(Part part);

	Optional<Part> findById(PartId id);

	Optional<Part> findByCode(String code);

	List<Part> findAll(boolean includeInactive);

	List<Part> findAtOrBelowMinimumStock();
}
