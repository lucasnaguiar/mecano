package br.dev.lucasaguiar.mecano.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

	@Test
	void normalizesToTwoDecimalPlaces() {
		assertThat(Money.of("10.5").amount()).isEqualByComparingTo("10.50");
		assertThat(Money.of("10.555")).isEqualTo(Money.of("10.56"));
		assertThat(Money.of("10")).isEqualTo(Money.of("10.00"));
	}

	@Test
	void addsAndMultiplies() {
		assertThat(Money.of("10.00").add(Money.of("2.50"))).isEqualTo(Money.of("12.50"));
		assertThat(Money.of("45.90").multiply(Quantity.of("3.750"))).isEqualTo(Money.of("172.12"));
		assertThat(Money.of("120.00").multiply(2)).isEqualTo(Money.of("240.00"));
		assertThat(Money.ZERO.amount()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void rejectsNegativeOrMissingAmount() {
		assertThatThrownBy(() -> Money.of("-0.01")).isInstanceOf(InvalidValueException.class)
				.hasMessage("O valor não pode ser negativo.");
		assertThatThrownBy(() -> new Money(null)).isInstanceOf(InvalidValueException.class);
	}
}
