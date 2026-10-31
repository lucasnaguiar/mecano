package br.dev.lucasaguiar.mecano.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class PageTest {

	@Test
	void validatesRequestedPage() {
		assertThat(PageQuery.first()).isEqualTo(new PageQuery(0, 20));
		assertThat(new PageQuery(3, 100).size()).isEqualTo(100);
		assertThatThrownBy(() -> new PageQuery(-1, 20)).isInstanceOf(InvalidValueException.class)
				.hasMessage("A página deve ser maior ou igual a zero.");
		assertThatThrownBy(() -> new PageQuery(0, 0)).isInstanceOf(InvalidValueException.class)
				.hasMessage("O tamanho da página deve estar entre 1 e 100.");
		assertThatThrownBy(() -> new PageQuery(0, 101)).isInstanceOf(InvalidValueException.class);
	}

	@Test
	void computesTotalPagesAndMapsContent() {
		var page = new Page<>(List.of(1, 2), 0, 2, 5);

		assertThat(page.totalPages()).isEqualTo(3);
		assertThat(new Page<>(List.of(), 0, 20, 0).totalPages()).isZero();
		assertThat(page.map(value -> "item " + value).content()).containsExactly("item 1", "item 2");
		assertThat(page.map(String::valueOf).totalElements()).isEqualTo(5);
	}
}
