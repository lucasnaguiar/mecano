package br.dev.lucasaguiar.mecano.shared.domain;

import java.time.Year;

/**
 * Ano-modelo do veículo, entre 1900 e o ano seguinte ao atual.
 */
public record ModelYear(int value) {

	public static final int MIN = 1900;

	public ModelYear {
		var max = Year.now().getValue() + 1;
		if (value < MIN || value > max) {
			throw new InvalidValueException("O ano do modelo deve estar entre " + MIN + " e " + max + ".");
		}
	}
}
