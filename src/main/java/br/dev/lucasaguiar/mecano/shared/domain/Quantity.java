package br.dev.lucasaguiar.mecano.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Quantidade positiva com até três casas decimais: inteira para peças, fracionada para insumos (litros, quilos etc.).
 */
public record Quantity(BigDecimal value) {

	private static final int SCALE = 3;

	public Quantity {
		if (value == null) {
			throw new InvalidValueException("A quantidade é obrigatória.");
		}
		if (value.signum() <= 0) {
			throw new InvalidValueException("A quantidade deve ser maior que zero.");
		}
		try {
			value = value.setScale(SCALE, RoundingMode.UNNECESSARY);
		} catch (ArithmeticException exception) {
			throw new InvalidValueException("A quantidade aceita no máximo 3 casas decimais.");
		}
	}

	public static Quantity of(int value) {
		return new Quantity(BigDecimal.valueOf(value));
	}

	public static Quantity of(String value) {
		return new Quantity(new BigDecimal(value));
	}

	public boolean isWhole() {
		return value.stripTrailingZeros().scale() <= 0;
	}
}
