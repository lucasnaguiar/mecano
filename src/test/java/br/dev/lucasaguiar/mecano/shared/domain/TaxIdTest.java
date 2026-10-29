package br.dev.lucasaguiar.mecano.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TaxIdTest {

	@Test
	void acceptsCpfWithOrWithoutPunctuation() {
		var taxId = new TaxId("529.982.247-25");

		assertThat(taxId.value()).isEqualTo("52998224725");
		assertThat(taxId.isCpf()).isTrue();
		assertThat(taxId.isCnpj()).isFalse();
	}

	@Test
	void acceptsNumericCnpj() {
		var taxId = new TaxId("11.222.333/0001-81");

		assertThat(taxId.value()).isEqualTo("11222333000181");
		assertThat(taxId.isCnpj()).isTrue();
	}

	@Test
	void acceptsAlphanumericCnpj() {
		assertThat(new TaxId("12.abc.345/01de-35").value()).isEqualTo("12ABC34501DE35");
	}

	@ParameterizedTest
	@ValueSource(strings = {"529.982.247-24", "111.111.111-11", "11.222.333/0001-82", "12ABC34501DE36",
			"00000000000000", "1234", "ABC.DEF.GHI-JK"})
	void rejectsInvalidDocuments(String value) {
		assertThatThrownBy(() -> new TaxId(value)).isInstanceOf(InvalidValueException.class)
				.hasMessage("CPF ou CNPJ inválido.");
	}

	@Test
	void rejectsBlankDocument() {
		assertThatThrownBy(() -> new TaxId(" ")).isInstanceOf(InvalidValueException.class)
				.hasMessage("O CPF ou CNPJ é obrigatório.");
	}
}
