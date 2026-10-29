package br.dev.lucasaguiar.mecano.shared.domain;

/**
 * Unidade em que um item de estoque é controlado.
 */
public enum UnitOfMeasure {
	UN(false),
	L(true),
	ML(true),
	KG(true),
	G(true),
	M(true);

	private final boolean fractional;

	UnitOfMeasure(boolean fractional) {
		this.fractional = fractional;
	}

	public boolean allowsFraction() {
		return fractional;
	}
}
