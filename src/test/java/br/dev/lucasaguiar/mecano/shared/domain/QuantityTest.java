package br.dev.lucasaguiar.mecano.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class QuantityTest {

	@Test
	void acceptsWholeAndFractionalQuantities() {
		assertThat(Quantity.of(2).isWhole()).isTrue();
		assertThat(Quantity.of("2.000").isWhole()).isTrue();
		assertThat(Quantity.of("0.750").isWhole()).isFalse();
		assertThat(Quantity.of("1.5")).isEqualTo(Quantity.of("1.500"));
	}

	@Test
	void rejectsZeroNegativeAndTooManyDecimals() {
		assertThatThrownBy(() -> Quantity.of(0)).isInstanceOf(InvalidValueException.class)
				.hasMessage("A quantidade deve ser maior que zero.");
		assertThatThrownBy(() -> Quantity.of("-1")).isInstanceOf(InvalidValueException.class);
		assertThatThrownBy(() -> Quantity.of("1.0001")).isInstanceOf(InvalidValueException.class)
				.hasMessage("A quantidade aceita no máximo 3 casas decimais.");
		assertThatThrownBy(() -> new Quantity(null)).isInstanceOf(InvalidValueException.class);
	}
}
