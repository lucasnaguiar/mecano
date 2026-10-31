package br.dev.lucasaguiar.mecano.shared.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.shared.domain.Page;
import br.dev.lucasaguiar.mecano.shared.domain.PageQuery;
import java.util.function.Function;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Conversão entre a paginação do domínio e a do Spring Data, usada pelos adaptadores de repositório.
 */
public final class Pages {

	private Pages() {
	}

	public static Pageable toPageable(PageQuery query) {
		return PageRequest.of(query.page(), query.size());
	}

	public static Pageable toPageable(PageQuery query, Sort sort) {
		return PageRequest.of(query.page(), query.size(), sort);
	}

	public static <E, T> Page<T> toPage(org.springframework.data.domain.Page<E> page, Function<E, T> mapper) {
		return new Page<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(),
				page.getTotalElements());
	}
}
