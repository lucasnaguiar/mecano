package br.dev.lucasaguiar.mecano.workorder.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.dev.lucasaguiar.mecano.shared.domain.InvalidValueException;
import org.junit.jupiter.api.Test;

class TrackingCodeTest {

	@Test
	void generatesDistinctCodesWithoutAmbiguousCharacters() {
		var first = TrackingCode.generate();
		var second = TrackingCode.generate();

		assertThat(first).isNotEqualTo(second);
		assertThat(first.value()).matches("[A-HJ-NP-Z2-9]{12}");
	}

	@Test
	void normalizesInput() {
		assertThat(new TrackingCode(" abcdefghjklm ").value()).isEqualTo("ABCDEFGHJKLM");
	}

	@Test
	void rejectsInvalidCodes() {
		assertThatThrownBy(() -> new TrackingCode("ABC")).isInstanceOf(InvalidValueException.class)
				.hasMessage("Código de acompanhamento inválido.");
		assertThatThrownBy(() -> new TrackingCode("ABCDEFGHJKL0")).isInstanceOf(InvalidValueException.class);
		assertThatThrownBy(() -> new TrackingCode(null)).isInstanceOf(InvalidValueException.class);
	}
}
