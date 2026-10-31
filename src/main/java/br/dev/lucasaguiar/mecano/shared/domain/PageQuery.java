package br.dev.lucasaguiar.mecano.shared.domain;

/**
 * Página solicitada de uma listagem: número da página (a partir de zero) e quantidade de itens por página.
 */
public record PageQuery(int page, int size) {

	public static final int DEFAULT_SIZE = 20;
	public static final int MAX_SIZE = 100;

	public PageQuery {
		if (page < 0) {
			throw new InvalidValueException("A página deve ser maior ou igual a zero.");
		}
		if (size < 1 || size > MAX_SIZE) {
			throw new InvalidValueException("O tamanho da página deve estar entre 1 e " + MAX_SIZE + ".");
		}
	}

	public static PageQuery first() {
		return new PageQuery(0, DEFAULT_SIZE);
	}
}
