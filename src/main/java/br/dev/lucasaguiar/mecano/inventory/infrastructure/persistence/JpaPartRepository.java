package br.dev.lucasaguiar.mecano.inventory.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.inventory.domain.Part;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.inventory.domain.PartRepository;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import br.dev.lucasaguiar.mecano.shared.domain.UnitOfMeasure;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Implementação JPA de PartRepository. A versão do domínio é repassada à entidade para o lock otimista.
 */
@Repository
public class JpaPartRepository implements PartRepository {

	private final SpringDataPartRepository springDataRepository;

	public JpaPartRepository(SpringDataPartRepository springDataRepository) {
		this.springDataRepository = springDataRepository;
	}

	@Override
	public Part save(Part part) {
		return toDomain(springDataRepository.saveAndFlush(toEntity(part)));
	}

	@Override
	public Optional<Part> findById(PartId id) {
		return springDataRepository.findById(id.value()).map(JpaPartRepository::toDomain);
	}

	@Override
	public Optional<Part> findByCode(String code) {
		return springDataRepository.findByCode(code).map(JpaPartRepository::toDomain);
	}

	@Override
	public List<Part> findAll(boolean includeInactive) {
		var entities = includeInactive
				? springDataRepository.findAllByOrderByName()
				: springDataRepository.findByActiveTrueOrderByName();
		return entities.stream().map(JpaPartRepository::toDomain).toList();
	}

	@Override
	public List<Part> findAtOrBelowMinimumStock() {
		return springDataRepository.findAtOrBelowMinimumStock().stream().map(JpaPartRepository::toDomain).toList();
	}

	private static PartEntity toEntity(Part part) {
		return new PartEntity(part.getId().value(), part.getCode(), part.getName(), part.getType(),
				part.getUnitOfMeasure().name(), part.getUnitPrice().amount(), part.getAvailableQuantity(),
				part.getReservedQuantity(), part.getMinimumStock(), part.isActive(), part.getVersion(),
				part.getCreatedAt(), part.getUpdatedAt());
	}

	private static Part toDomain(PartEntity entity) {
		return Part.restore(new PartId(entity.getId()), entity.getCode(), entity.getName(), entity.getType(),
				UnitOfMeasure.valueOf(entity.getUnitOfMeasure()), new Money(entity.getUnitPrice()),
				entity.getAvailableQuantity(), entity.getReservedQuantity(), entity.getMinimumStock(),
				entity.isActive(), entity.getVersion(), entity.getCreatedAt(), entity.getUpdatedAt());
	}
}
