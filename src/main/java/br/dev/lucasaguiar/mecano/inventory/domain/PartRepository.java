package br.dev.lucasaguiar.mecano.inventory.domain;

import br.dev.lucasaguiar.mecano.shared.domain.Page;
import br.dev.lucasaguiar.mecano.shared.domain.PageQuery;
import java.util.Optional;

/**
 * Repositório do agregado Part.
 */
public interface PartRepository {

	Part save(Part part);

	Optional<Part> findById(PartId id);

	Optional<Part> findByCode(String code);

	/** Lista as peças em ordem alfabética, opcionalmente incluindo as desativadas. */
	Page<Part> findAll(boolean includeInactive, PageQuery query);

	/** Lista as peças ativas cujo saldo disponível está no estoque mínimo ou abaixo dele. */
	Page<Part> findAtOrBelowMinimumStock(PageQuery query);
}
