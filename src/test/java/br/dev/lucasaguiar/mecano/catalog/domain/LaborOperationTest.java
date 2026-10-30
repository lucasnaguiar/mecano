package br.dev.lucasaguiar.mecano.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.dev.lucasaguiar.mecano.shared.domain.BusinessRuleException;
import br.dev.lucasaguiar.mecano.shared.domain.InvalidValueException;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class LaborOperationTest {

	private static final Instant NOW = Instant.parse("2026-01-15T12:00:00Z");

	@Test
	void registerCreatesActiveOperation() {
		var operation = LaborOperation.register(" Troca de óleo ", " ", Money.of("120"), Duration.ofMinutes(45), NOW);

		assertThat(operation.getName()).isEqualTo("Troca de óleo");
		assertThat(operation.getDescription()).isNull();
		assertThat(operation.isActive()).isTrue();
		assertThatCode(operation::ensureActive).doesNotThrowAnyException();
	}

	@Test
	void deactivatedOperationCannotBeUsed() {
		var operation = LaborOperation.register("Alinhamento", null, Money.of("80"), Duration.ofMinutes(30), NOW);

		operation.deactivate(NOW.plusSeconds(1));

		assertThat(operation.isActive()).isFalse();
		assertThatThrownBy(operation::ensureActive).isInstanceOf(BusinessRuleException.class)
				.hasMessageContaining("Alinhamento");
		operation.activate(NOW.plusSeconds(2));
		assertThat(operation.isActive()).isTrue();
	}

	@Test
	void updateChangesPriceAndDuration() {
		var operation = LaborOperation.restore(LaborOperationId.newId(), "Alinhamento", null, Money.of("80"),
				Duration.ofMinutes(30), true, NOW, NOW);

		operation.update("Alinhamento 3D", "Com laser", Money.of("95"), Duration.ofMinutes(40), NOW.plusSeconds(1));

		assertThat(operation.getPrice()).isEqualTo(Money.of("95"));
		assertThat(operation.getEstimatedDuration()).isEqualTo(Duration.ofMinutes(40));
		assertThat(operation.getUpdatedAt()).isEqualTo(NOW.plusSeconds(1));
	}

	@Test
	void rejectsInvalidData() {
		assertThatThrownBy(() -> LaborOperation.register("", null, Money.of("1"), Duration.ofMinutes(1), NOW))
				.isInstanceOf(InvalidValueException.class);
		assertThatThrownBy(() -> LaborOperation.register("X", null, null, Duration.ofMinutes(1), NOW))
				.isInstanceOf(InvalidValueException.class);
		assertThatThrownBy(() -> LaborOperation.register("X", null, Money.of("1"), Duration.ofSeconds(30), NOW))
				.isInstanceOf(InvalidValueException.class)
				.hasMessage("O tempo estimado deve ser de pelo menos 1 minuto.");
	}
}
