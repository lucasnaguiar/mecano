package br.dev.lucasaguiar.mecano.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.dev.lucasaguiar.mecano.inventory.domain.event.MinimumStockReached;
import br.dev.lucasaguiar.mecano.inventory.domain.event.PartPriceChanged;
import br.dev.lucasaguiar.mecano.inventory.domain.event.PartRegistered;
import br.dev.lucasaguiar.mecano.inventory.domain.event.PartsReserved;
import br.dev.lucasaguiar.mecano.inventory.domain.event.ReservationReleased;
import br.dev.lucasaguiar.mecano.inventory.domain.event.StockDecreased;
import br.dev.lucasaguiar.mecano.inventory.domain.event.StockEntryRegistered;
import br.dev.lucasaguiar.mecano.shared.domain.BusinessRuleException;
import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.shared.domain.InvalidValueException;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import br.dev.lucasaguiar.mecano.shared.domain.Quantity;
import br.dev.lucasaguiar.mecano.shared.domain.UnitOfMeasure;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PartTest {

	private static final Instant NOW = Instant.parse("2026-01-15T12:00:00Z");
	private static final UUID WORK_ORDER = UUID.randomUUID();
	private static final UserId USER = UserId.newId();

	private Part oil;
	private Part filter;

	@BeforeEach
	void setUp() {
		oil = Part.register(" oleo-5w30 ", "Óleo 5W30", PartType.SUPPLY, UnitOfMeasure.L, Money.of("45.90"),
				new BigDecimal("2"), NOW);
		filter = Part.register("FILTRO-01", "Filtro de óleo", PartType.PART, UnitOfMeasure.UN, Money.of("35"),
				new BigDecimal("1"), NOW);
	}

	@Test
	void registerStartsWithEmptyStock() {
		assertThat(oil.getCode()).isEqualTo("OLEO-5W30");
		assertThat(oil.getAvailableQuantity()).isZero();
		assertThat(oil.getReservedQuantity()).isZero();
		assertThat(oil.isActive()).isTrue();
		assertThat(oil.getVersion()).isNull();
		assertThat(oil.pullEvents()).singleElement().isInstanceOf(PartRegistered.class);
	}

	@Test
	void entryIncreasesAvailableAndRecordsMovement() {
		oil.pullEvents();

		var movement = oil.registerEntry(Quantity.of("10.5"), USER, NOW);

		assertThat(oil.getAvailableQuantity()).isEqualByComparingTo("10.5");
		assertThat(movement.getType()).isEqualTo(StockMovementType.ENTRY);
		assertThat(movement.getUserId()).isEqualTo(USER);
		assertThat(movement.getWorkOrderId()).isNull();
		assertThat(oil.pullEvents()).singleElement().isInstanceOf(StockEntryRegistered.class);
	}

	@Test
	void reserveMovesFromAvailableToReserved() {
		oil.registerEntry(Quantity.of(10), USER, NOW);
		oil.pullEvents();

		var movement = oil.reserve(Quantity.of("3.750"), WORK_ORDER, NOW);

		assertThat(oil.getAvailableQuantity()).isEqualByComparingTo("6.25");
		assertThat(oil.getReservedQuantity()).isEqualByComparingTo("3.75");
		assertThat(movement.getType()).isEqualTo(StockMovementType.RESERVATION);
		assertThat(movement.getWorkOrderId()).isEqualTo(WORK_ORDER);
		assertThat(oil.pullEvents()).singleElement().isInstanceOf(PartsReserved.class);
	}

	@Test
	void reserveRaisesMinimumStockReachedOnlyWhenCrossingTheMinimum() {
		oil.registerEntry(Quantity.of(5), USER, NOW);
		oil.pullEvents();

		oil.reserve(Quantity.of(3), WORK_ORDER, NOW);
		assertThat(oil.pullEvents()).extracting(DomainEvent::getClass)
				.containsExactly(PartsReserved.class, MinimumStockReached.class);
		assertThat(oil.isAtOrBelowMinimumStock()).isTrue();

		oil.reserve(Quantity.of(1), WORK_ORDER, NOW);
		assertThat(oil.pullEvents()).extracting(DomainEvent::getClass).containsExactly(PartsReserved.class);
	}

	@Test
	void reserveFailsWhenStockIsInsufficient() {
		filter.registerEntry(Quantity.of(1), USER, NOW);

		assertThatThrownBy(() -> filter.reserve(Quantity.of(2), WORK_ORDER, NOW))
				.isInstanceOf(BusinessRuleException.class)
				.hasMessage("Estoque insuficiente de \"Filtro de óleo\": disponível 1 UN, necessário 2 UN.");
		assertThat(filter.getAvailableQuantity()).isEqualByComparingTo("1");
	}

	@Test
	void releaseReturnsReservedQuantity() {
		oil.registerEntry(Quantity.of(10), USER, NOW);
		oil.reserve(Quantity.of(4), WORK_ORDER, NOW);
		oil.pullEvents();

		var movement = oil.releaseReservation(Quantity.of(4), WORK_ORDER, NOW);

		assertThat(oil.getAvailableQuantity()).isEqualByComparingTo("10");
		assertThat(oil.getReservedQuantity()).isZero();
		assertThat(movement.getType()).isEqualTo(StockMovementType.RELEASE);
		assertThat(oil.pullEvents()).singleElement().isInstanceOf(ReservationReleased.class);
	}

	@Test
	void decreaseRemovesReservedQuantity() {
		oil.registerEntry(Quantity.of(10), USER, NOW);
		oil.reserve(Quantity.of(4), WORK_ORDER, NOW);
		oil.pullEvents();

		var movement = oil.decrease(Quantity.of(4), WORK_ORDER, NOW);

		assertThat(oil.getAvailableQuantity()).isEqualByComparingTo("6");
		assertThat(oil.getReservedQuantity()).isZero();
		assertThat(movement.getType()).isEqualTo(StockMovementType.WITHDRAWAL);
		assertThat(oil.pullEvents()).singleElement().isInstanceOf(StockDecreased.class);
	}

	@Test
	void cannotDecreaseOrReleaseMoreThanReserved() {
		oil.registerEntry(Quantity.of(10), USER, NOW);
		oil.reserve(Quantity.of(1), WORK_ORDER, NOW);

		assertThatThrownBy(() -> oil.decrease(Quantity.of(2), WORK_ORDER, NOW))
				.isInstanceOf(BusinessRuleException.class);
		assertThatThrownBy(() -> oil.releaseReservation(Quantity.of(2), WORK_ORDER, NOW))
				.isInstanceOf(BusinessRuleException.class);
		assertThatThrownBy(() -> oil.decrease(Quantity.of(1), null, NOW))
				.isInstanceOf(InvalidValueException.class);
	}

	@Test
	void partsInUnitsRejectFractions() {
		assertThatThrownBy(() -> filter.registerEntry(Quantity.of("1.5"), USER, NOW))
				.isInstanceOf(InvalidValueException.class)
				.hasMessageContaining("não aceita quantidade fracionada");
		assertThatThrownBy(() -> filter.update("Filtro", new BigDecimal("0.5"), NOW))
				.isInstanceOf(InvalidValueException.class);
		assertThatThrownBy(() -> Part.register("X", "Mangueira", PartType.PART, UnitOfMeasure.M, Money.of("1"),
				BigDecimal.ZERO, NOW))
				.isInstanceOf(InvalidValueException.class)
				.hasMessageContaining("Peças são controladas por unidade");
	}

	@Test
	void changePriceRaisesEventOnlyWhenPriceChanges() {
		oil.pullEvents();

		oil.changePrice(Money.of("45.90"), NOW);
		assertThat(oil.pullEvents()).isEmpty();

		oil.changePrice(Money.of("49.90"), NOW);
		assertThat(oil.getUnitPrice()).isEqualTo(Money.of("49.90"));
		assertThat(oil.pullEvents()).singleElement().isInstanceOf(PartPriceChanged.class);
	}

	@Test
	void deactivatedPartCannotBeUsed() {
		assertThatCode(oil::ensureActive).doesNotThrowAnyException();

		oil.deactivate(NOW);

		assertThat(oil.isActive()).isFalse();
		assertThatThrownBy(oil::ensureActive).isInstanceOf(BusinessRuleException.class);
	}

	@Test
	void updateValidatesMinimumStock() {
		oil.update("Óleo sintético 5W30", new BigDecimal("1.5"), NOW);

		assertThat(oil.getName()).isEqualTo("Óleo sintético 5W30");
		assertThat(oil.getMinimumStock()).isEqualByComparingTo("1.5");
		assertThatThrownBy(() -> oil.update("Óleo", new BigDecimal("-1"), NOW))
				.isInstanceOf(InvalidValueException.class);
		assertThatThrownBy(() -> oil.update("Óleo", null, NOW)).isInstanceOf(InvalidValueException.class);
	}

	@Test
	void restoreKeepsStateAndVersion() {
		var id = PartId.newId();
		var part = Part.restore(id, "X-1", "Item", PartType.SUPPLY, UnitOfMeasure.KG, Money.of("10"),
				new BigDecimal("3"), new BigDecimal("1"), BigDecimal.ZERO, false, 7L, NOW, NOW);

		assertThat(part.getId()).isEqualTo(id);
		assertThat(part.getVersion()).isEqualTo(7L);
		assertThat(part.getType()).isEqualTo(PartType.SUPPLY);
		assertThat(part.getUnitOfMeasure()).isEqualTo(UnitOfMeasure.KG);
		assertThat(part.getCreatedAt()).isEqualTo(NOW);
		assertThat(part.getUpdatedAt()).isEqualTo(NOW);
		assertThat(part.pullEvents()).isEmpty();
	}

	@Test
	void restoredMovementKeepsData() {
		var id = UUID.randomUUID();
		var movement = StockMovement.restore(id, oil.getId(), StockMovementType.ENTRY, Quantity.of(1), null, USER, NOW);

		assertThat(movement.getId()).isEqualTo(id);
		assertThat(movement.getPartId()).isEqualTo(oil.getId());
		assertThat(movement.getQuantity()).isEqualTo(Quantity.of(1));
		assertThat(movement.getOccurredAt()).isEqualTo(NOW);
	}
}
