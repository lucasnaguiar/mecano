package br.dev.lucasaguiar.mecano.shared.presentation;

import br.dev.lucasaguiar.mecano.shared.domain.Page;
import java.util.List;
import java.util.function.Function;

/**
 * Página de uma listagem devolvida pela API.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

	public static <S, T> PageResponse<T> from(Page<S> page, Function<? super S, ? extends T> mapper) {
		Page<T> mapped = page.map(mapper);
		return new PageResponse<>(mapped.content(), mapped.page(), mapped.size(), mapped.totalElements(),
				mapped.totalPages());
	}
}
