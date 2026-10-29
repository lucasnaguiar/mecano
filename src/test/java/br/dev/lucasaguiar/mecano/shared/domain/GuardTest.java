package br.dev.lucasaguiar.mecano.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class GuardTest {

	@Test
	void validatesRequiredValues() {
		assertThat(Guard.requireText("  Ana  ", "obrigatório")).isEqualTo("Ana");
		assertThat(Guard.requireNonNull(1, "obrigatório")).isEqualTo(1);
		assertThatThrownBy(() -> Guard.requireText(" ", "Nome obrigatório.")).hasMessage("Nome obrigatório.");
		assertThatThrownBy(() -> Guard.requireNonNull(null, "Valor obrigatório.")).hasMessage("Valor obrigatório.");
	}

	@Test
	void normalizesOptionalText() {
		assertThat(Guard.optionalText("  ")).isNull();
		assertThat(Guard.optionalText(null)).isNull();
		assertThat(Guard.optionalText(" obs ")).isEqualTo("obs");
	}

	@Test
	void unitsOfMeasureDefineWhetherFractionsAreAllowed() {
		assertThat(UnitOfMeasure.UN.allowsFraction()).isFalse();
		assertThat(UnitOfMeasure.L.allowsFraction()).isTrue();
	}
}
