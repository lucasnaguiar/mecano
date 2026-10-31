package br.dev.lucasaguiar.mecano.inventory.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.inventory.domain.Part;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.inventory.domain.PartRepository;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import br.dev.lucasaguiar.mecano.shared.domain.Page;
import br.dev.lucasaguiar.mecano.shared.domain.PageQuery;
import br.dev.lucasaguiar.mecano.shared.domain.UnitOfMeasure;
import br.dev.lucasaguiar.mecano.shared.infrastructure.persistence.Pages;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/**
 * Implementação JPA de PartRepository. A versão do domínio é repassada à entidade para o lock otimista.
 */
@Repository
public class JpaPartRepository implements PartRepository {

	private static final Sort BY_NAME = Sort.by("name", "id");

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
	public Page<Part> findAll(boolean includeInactive, PageQuery query) {
		var pageable = Pages.toPageable(query, BY_NAME);
		var entities = includeInactive
				? springDataRepository.findAll(pageable)
				: springDataRepository.findByActiveTrue(pageable);
		return Pages.toPage(entities, JpaPartRepository::toDomain);
	}

	@Override
	public Page<Part> findAtOrBelowMinimumStock(PageQuery query) {
		return Pages.toPage(springDataRepository.findAtOrBelowMinimumStock(Pages.toPageable(query, BY_NAME)),
				JpaPartRepository::toDomain);
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
