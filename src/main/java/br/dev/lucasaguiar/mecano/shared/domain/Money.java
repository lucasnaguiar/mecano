package br.dev.lucasaguiar.mecano.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Valor monetário em reais (BRL), com duas casas decimais e nunca negativo.
 */
public record Money(BigDecimal amount) {

	public static final Money ZERO = new Money(BigDecimal.ZERO);

	public Money {
		if (amount == null) {
			throw new InvalidValueException("O valor é obrigatório.");
		}
		if (amount.signum() < 0) {
			throw new InvalidValueException("O valor não pode ser negativo.");
		}
		amount = amount.setScale(2, RoundingMode.HALF_EVEN);
	}

	public static Money of(String amount) {
		return new Money(new BigDecimal(amount));
	}

	public Money add(Money other) {
		return new Money(amount.add(other.amount));
	}

	public Money multiply(Quantity quantity) {
		return new Money(amount.multiply(quantity.value()));
	}

	public Money multiply(int factor) {
		return new Money(amount.multiply(BigDecimal.valueOf(factor)));
	}
}
