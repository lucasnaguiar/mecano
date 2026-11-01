package br.dev.lucasaguiar.mecano.shared.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PersonalDataMaskerTest {

	@Test
	void keepsFirstNameAndInitials() {
		assertThat(PersonalDataMasker.name("Maria Oliveira santos")).isEqualTo("Maria O. S.");
		assertThat(PersonalDataMasker.name("  Pedro  ")).isEqualTo("Pedro");
		assertThat(PersonalDataMasker.name(" ")).isEqualTo("***");
		assertThat(PersonalDataMasker.name(null)).isEqualTo("***");
	}

	@Test
	void hidesTheLocalPartOfEmail() {
		assertThat(PersonalDataMasker.email("maria@email.com")).isEqualTo("m***@email.com");
		assertThat(PersonalDataMasker.email("invalido")).isEqualTo("***");
		assertThat(PersonalDataMasker.email(null)).isEqualTo("***");
	}

	@Test
	void showsOnlyTheLastCharacters() {
		assertThat(PersonalDataMasker.lastDigits("ABCDEFGHJKLM", 4)).isEqualTo("***JKLM");
		assertThat(PersonalDataMasker.lastDigits("ABC", 4)).isEqualTo("***");
		assertThat(PersonalDataMasker.lastDigits(null, 4)).isEqualTo("***");
	}
}
