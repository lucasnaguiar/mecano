package br.dev.lucasaguiar.mecano.shared.domain;

import java.util.List;
import java.util.function.Function;

/**
 * Página de uma listagem com o total de itens encontrados.
 */
public record Page<T>(List<T> content, int page, int size, long totalElements) {

	public Page {
		content = List.copyOf(content);
	}

	public int totalPages() {
		return (int) ((totalElements + size - 1) / size);
	}

	public <R> Page<R> map(Function<? super T, ? extends R> mapper) {
		return new Page<>(content.stream().<R>map(mapper).toList(), page, size, totalElements);
	}
}
