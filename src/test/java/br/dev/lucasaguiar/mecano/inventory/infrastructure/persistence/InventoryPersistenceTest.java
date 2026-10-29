package br.dev.lucasaguiar.mecano.inventory.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import br.dev.lucasaguiar.mecano.PersistenceTest;
import br.dev.lucasaguiar.mecano.inventory.domain.PartType;
import br.dev.lucasaguiar.mecano.inventory.domain.StockMovementType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@PersistenceTest
class InventoryPersistenceTest {

	private static final Instant NOW = Instant.parse("2026-01-15T12:00:00Z");

	@Autowired
	private SpringDataPartRepository parts;

	@Autowired
	private SpringDataStockMovementRepository stockMovements;

	@Test
	void keepsFractionalQuantitiesAndStartsVersionAtZero() {
		var saved = parts.saveAndFlush(part("OLEO-5W30", PartType.SUPPLY, "L", "10.750", "2"));

		assertThat(saved.getVersion()).isZero();
		assertThat(parts.findByCode("OLEO-5W30")).get()
				.extracting(PartEntity::getAvailableQuantity)
				.isEqualTo(new BigDecimal("10.750"));
	}

	@Test
	void findsPartsAtOrBelowMinimumStock() {
		parts.save(part("FILTRO-01", PartType.PART, "UN", "2", "2"));
		parts.save(part("PASTILHA-01", PartType.PART, "UN", "1", "4"));
		parts.save(part("VELA-01", PartType.PART, "UN", "20", "4"));

		assertThat(parts.findAtOrBelowMinimumStock())
				.extracting(PartEntity::getCode)
				.containsExactly("FILTRO-01", "PASTILHA-01");
	}

	@Test
	void listsMovementsOfPartFromNewestToOldest() {
		var part = parts.save(part("FILTRO-02", PartType.PART, "UN", "10", "1"));
		stockMovements.save(movement(part.getId(), StockMovementType.ENTRY, NOW.minusSeconds(60)));
		stockMovements.save(movement(part.getId(), StockMovementType.ENTRY, NOW));

		assertThat(stockMovements.findByPartIdOrderByOccurredAtDesc(part.getId()))
				.extracting(StockMovementEntity::getOccurredAt)
				.containsExactly(NOW, NOW.minusSeconds(60));
	}

	private static PartEntity part(String code, PartType type, String unit, String available, String minimum) {
		return new PartEntity(UUID.randomUUID(), code, "Item " + code, type, unit, new BigDecimal("35.00"),
				new BigDecimal(available), BigDecimal.ZERO, new BigDecimal(minimum), true, null, NOW, NOW);
	}

	private static StockMovementEntity movement(UUID partId, StockMovementType type, Instant occurredAt) {
		return new StockMovementEntity(UUID.randomUUID(), partId, null, null, type, BigDecimal.ONE, occurredAt);
	}
}
