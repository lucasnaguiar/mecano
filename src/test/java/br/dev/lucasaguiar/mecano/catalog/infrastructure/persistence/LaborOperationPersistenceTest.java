package br.dev.lucasaguiar.mecano.catalog.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import br.dev.lucasaguiar.mecano.PersistenceTest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@PersistenceTest
class LaborOperationPersistenceTest {

	private static final Instant NOW = Instant.parse("2026-01-15T12:00:00Z");

	@Autowired
	private SpringDataLaborOperationRepository laborOperations;

	@Test
	void listsOnlyActiveOperationsOrderedByName() {
		laborOperations.save(laborOperation("Troca de óleo", true));
		laborOperations.save(laborOperation("Alinhamento", true));
		laborOperations.save(laborOperation("Balanceamento", false));

		assertThat(laborOperations.findByActiveTrueOrderByName())
				.extracting(LaborOperationEntity::getName)
				.containsExactly("Alinhamento", "Troca de óleo");
		assertThat(laborOperations.existsByName("Balanceamento")).isTrue();
	}

	private static LaborOperationEntity laborOperation(String name, boolean active) {
		return new LaborOperationEntity(UUID.randomUUID(), name, null, new BigDecimal("120.00"), 60, active, NOW, NOW);
	}
}
