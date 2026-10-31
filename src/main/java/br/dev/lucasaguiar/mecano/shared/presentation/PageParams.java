package br.dev.lucasaguiar.mecano.shared.presentation;

import br.dev.lucasaguiar.mecano.shared.domain.PageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;

/**
 * Parâmetros de paginação recebidos na query string das listagens.
 */
@ParameterObject
public record PageParams(
		@Parameter(description = "Número da página, a partir de 0", example = "0") Integer page,
		@Parameter(description = "Itens por página, de 1 a 100", example = "20") Integer size) {

	public PageQuery toQuery() {
		return new PageQuery(page == null ? 0 : page, size == null ? PageQuery.DEFAULT_SIZE : size);
	}
}
