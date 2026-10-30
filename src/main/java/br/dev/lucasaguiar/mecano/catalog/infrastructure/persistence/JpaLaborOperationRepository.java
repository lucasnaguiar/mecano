package br.dev.lucasaguiar.mecano.catalog.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperation;
import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationId;
import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationRepository;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/**
 * Implementação JPA de LaborOperationRepository.
 */
@Repository
public class JpaLaborOperationRepository implements LaborOperationRepository {

	private final SpringDataLaborOperationRepository springDataRepository;

	public JpaLaborOperationRepository(SpringDataLaborOperationRepository springDataRepository) {
		this.springDataRepository = springDataRepository;
	}

	@Override
	public LaborOperation save(LaborOperation laborOperation) {
		return toDomain(springDataRepository.save(toEntity(laborOperation)));
	}

	@Override
	public Optional<LaborOperation> findById(LaborOperationId id) {
		return springDataRepository.findById(id.value()).map(JpaLaborOperationRepository::toDomain);
	}

	@Override
	public Optional<LaborOperation> findByName(String name) {
		return springDataRepository.findByName(name).map(JpaLaborOperationRepository::toDomain);
	}

	@Override
	public List<LaborOperation> findAll(boolean includeInactive) {
		var entities = includeInactive
				? springDataRepository.findAll(Sort.by("name"))
				: springDataRepository.findByActiveTrueOrderByName();
		return entities.stream().map(JpaLaborOperationRepository::toDomain).toList();
	}

	private static LaborOperationEntity toEntity(LaborOperation operation) {
		return new LaborOperationEntity(operation.getId().value(), operation.getName(), operation.getDescription(),
				operation.getPrice().amount(), (int) operation.getEstimatedDuration().toMinutes(), operation.isActive(),
				operation.getCreatedAt(), operation.getUpdatedAt());
	}

	private static LaborOperation toDomain(LaborOperationEntity entity) {
		return LaborOperation.restore(new LaborOperationId(entity.getId()), entity.getName(), entity.getDescription(),
				new Money(entity.getPrice()), Duration.ofMinutes(entity.getEstimatedDurationMinutes()),
				entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt());
	}
}
