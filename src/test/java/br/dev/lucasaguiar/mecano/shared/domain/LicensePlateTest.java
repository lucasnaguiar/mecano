package br.dev.lucasaguiar.mecano.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LicensePlateTest {

	@Test
	void normalizesOldFormat() {
		var plate = new LicensePlate("abc-1234");

		assertThat(plate.value()).isEqualTo("ABC1234");
		assertThat(plate.isMercosul()).isFalse();
	}

	@Test
	void acceptsMercosulFormat() {
		var plate = new LicensePlate("BRA2E19");

		assertThat(plate.value()).isEqualTo("BRA2E19");
		assertThat(plate.isMercosul()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"AB1234", "ABCD123", "1BC1234", "ABC12345", "ABC1DD3"})
	void rejectsInvalidFormats(String value) {
		assertThatThrownBy(() -> new LicensePlate(value)).isInstanceOf(InvalidValueException.class);
	}

	@Test
	void rejectsBlankPlate() {
		assertThatThrownBy(() -> new LicensePlate(null)).isInstanceOf(InvalidValueException.class)
				.hasMessage("A placa é obrigatória.");
	}
}
