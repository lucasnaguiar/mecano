package br.dev.lucasaguiar.mecano.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Year;
import org.junit.jupiter.api.Test;

class ModelYearTest {

	@Test
	void acceptsYearsFrom1900UntilNextYear() {
		var nextYear = Year.now().getValue() + 1;

		assertThat(new ModelYear(1900).value()).isEqualTo(1900);
		assertThat(new ModelYear(nextYear).value()).isEqualTo(nextYear);
	}

	@Test
	void rejectsYearsOutOfRange() {
		assertThatThrownBy(() -> new ModelYear(1899)).isInstanceOf(InvalidValueException.class);
		assertThatThrownBy(() -> new ModelYear(Year.now().getValue() + 2)).isInstanceOf(InvalidValueException.class);
	}
}
