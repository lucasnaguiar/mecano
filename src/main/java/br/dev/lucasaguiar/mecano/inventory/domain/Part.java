package br.dev.lucasaguiar.mecano.inventory.domain;

import br.dev.lucasaguiar.mecano.inventory.domain.event.MinimumStockReached;
import br.dev.lucasaguiar.mecano.inventory.domain.event.PartPriceChanged;
import br.dev.lucasaguiar.mecano.inventory.domain.event.PartRegistered;
import br.dev.lucasaguiar.mecano.inventory.domain.event.PartsReserved;
import br.dev.lucasaguiar.mecano.inventory.domain.event.ReservationReleased;
import br.dev.lucasaguiar.mecano.inventory.domain.event.StockDecreased;
import br.dev.lucasaguiar.mecano.inventory.domain.event.StockEntryRegistered;
import br.dev.lucasaguiar.mecano.shared.domain.AggregateRoot;
import br.dev.lucasaguiar.mecano.shared.domain.BusinessRuleException;
import br.dev.lucasaguiar.mecano.shared.domain.Guard;
import br.dev.lucasaguiar.mecano.shared.domain.InvalidValueException;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import br.dev.lucasaguiar.mecano.shared.domain.Quantity;
import br.dev.lucasaguiar.mecano.shared.domain.UnitOfMeasure;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root: peça (PART, sempre por unidade) ou insumo (SUPPLY, aceita quantidade fracionada) do estoque.
 * Cada operação de estoque devolve a StockMovement correspondente, que funciona como extrato.
 */
public class Part extends AggregateRoot {

	private final PartId id;
	private final String code;
	private final PartType type;
	private final UnitOfMeasure unitOfMeasure;
	private final Long version;
	private final Instant createdAt;
	private String name;
	private Money unitPrice;
	private BigDecimal availableQuantity;
	private BigDecimal reservedQuantity;
	private BigDecimal minimumStock;
	private boolean active;
	private Instant updatedAt;

	private Part(PartId id, String code, String name, PartType type, UnitOfMeasure unitOfMeasure, Money unitPrice,
			BigDecimal availableQuantity, BigDecimal reservedQuantity, BigDecimal minimumStock, boolean active,
			Long version, Instant createdAt, Instant updatedAt) {
		this.id = Objects.requireNonNull(id);
		this.code = Guard.requireText(code, "O código da peça é obrigatório.").toUpperCase(Locale.ROOT);
		this.type = Guard.requireNonNull(type, "O tipo (PART ou SUPPLY) é obrigatório.");
		this.unitOfMeasure = Guard.requireNonNull(unitOfMeasure, "A unidade de medida é obrigatória.");
		if (type == PartType.PART && unitOfMeasure != UnitOfMeasure.UN) {
			throw new InvalidValueException("Peças são controladas por unidade (UN). Use SUPPLY para insumos.");
		}
		this.unitPrice = Guard.requireNonNull(unitPrice, "O preço é obrigatório.");
		this.availableQuantity = Objects.requireNonNull(availableQuantity);
		this.reservedQuantity = Objects.requireNonNull(reservedQuantity);
		this.active = active;
		this.version = version;
		this.createdAt = Objects.requireNonNull(createdAt);
		changeData(name, minimumStock, updatedAt);
	}

	public static Part register(String code, String name, PartType type, UnitOfMeasure unitOfMeasure, Money unitPrice,
			BigDecimal minimumStock, Instant now) {
		var part = new Part(PartId.newId(), code, name, type, unitOfMeasure, unitPrice, BigDecimal.ZERO,
				BigDecimal.ZERO, minimumStock, true, null, now, now);
		part.registerEvent(new PartRegistered(part.id, now));
		return part;
	}

	public static Part restore(PartId id, String code, String name, PartType type, UnitOfMeasure unitOfMeasure,
			Money unitPrice, BigDecimal availableQuantity, BigDecimal reservedQuantity, BigDecimal minimumStock,
			boolean active, Long version, Instant createdAt, Instant updatedAt) {
		return new Part(id, code, name, type, unitOfMeasure, unitPrice, availableQuantity, reservedQuantity,
				minimumStock, active, version, createdAt, updatedAt);
	}

	public void update(String name, BigDecimal minimumStock, Instant now) {
		changeData(name, minimumStock, now);
	}

	public void changePrice(Money newPrice, Instant now) {
		Guard.requireNonNull(newPrice, "O preço é obrigatório.");
		if (newPrice.equals(unitPrice)) {
			return;
		}
		unitPrice = newPrice;
		updatedAt = now;
		registerEvent(new PartPriceChanged(id, now));
	}

	public void deactivate(Instant now) {
		active = false;
		updatedAt = now;
	}

	public void ensureActive() {
		if (!active) {
			throw new BusinessRuleException("O item \"" + name + "\" está inativo e não pode ser incluído.");
		}
	}

	/** Recebimento de novas unidades. */
	public StockMovement registerEntry(Quantity quantity, UserId registeredBy, Instant now) {
		requireAcceptedQuantity(quantity);
		availableQuantity = availableQuantity.add(quantity.value());
		updatedAt = now;
		registerEvent(new StockEntryRegistered(id, now));
		return StockMovement.record(id, StockMovementType.ENTRY, quantity, null, registeredBy, now);
	}

	/** Separa a quantidade para uma OS aprovada: sai do disponível e vai para o reservado. */
	public StockMovement reserve(Quantity quantity, UUID workOrderId, Instant now) {
		requireAcceptedQuantity(quantity);
		if (availableQuantity.compareTo(quantity.value()) < 0) {
			throw new BusinessRuleException("Estoque insuficiente de \"%s\": disponível %s %s, necessário %s %s."
					.formatted(name, availableQuantity.stripTrailingZeros().toPlainString(), unitOfMeasure,
							quantity.value().stripTrailingZeros().toPlainString(), unitOfMeasure));
		}
		var wasAboveMinimum = !isAtOrBelowMinimumStock();
		availableQuantity = availableQuantity.subtract(quantity.value());
		reservedQuantity = reservedQuantity.add(quantity.value());
		updatedAt = now;
		registerEvent(new PartsReserved(id, now));
		if (wasAboveMinimum && isAtOrBelowMinimumStock()) {
			registerEvent(new MinimumStockReached(id, now));
		}
		return StockMovement.record(id, StockMovementType.RESERVATION, quantity, requireWorkOrder(workOrderId), null,
				now);
	}

	/** Devolve ao disponível uma quantidade reservada. */
	public StockMovement releaseReservation(Quantity quantity, UUID workOrderId, Instant now) {
		requireReserved(quantity);
		reservedQuantity = reservedQuantity.subtract(quantity.value());
		availableQuantity = availableQuantity.add(quantity.value());
		updatedAt = now;
		registerEvent(new ReservationReleased(id, now));
		return StockMovement.record(id, StockMovementType.RELEASE, quantity, requireWorkOrder(workOrderId), null, now);
	}

	/** Saída definitiva da quantidade reservada, quando a execução começa. */
	public StockMovement decrease(Quantity quantity, UUID workOrderId, Instant now) {
		requireReserved(quantity);
		reservedQuantity = reservedQuantity.subtract(quantity.value());
		updatedAt = now;
		registerEvent(new StockDecreased(id, now));
		return StockMovement.record(id, StockMovementType.WITHDRAWAL, quantity, requireWorkOrder(workOrderId), null,
				now);
	}

	public boolean isAtOrBelowMinimumStock() {
		return availableQuantity.compareTo(minimumStock) <= 0;
	}

	public void requireAcceptedQuantity(Quantity quantity) {
		Guard.requireNonNull(quantity, "A quantidade é obrigatória.");
		if (!unitOfMeasure.allowsFraction() && !quantity.isWhole()) {
			throw new InvalidValueException("\"" + name + "\" é controlado em " + unitOfMeasure
					+ " e não aceita quantidade fracionada.");
		}
	}

	private void requireReserved(Quantity quantity) {
		requireAcceptedQuantity(quantity);
		if (reservedQuantity.compareTo(quantity.value()) < 0) {
			throw new BusinessRuleException("Não há quantidade reservada suficiente de \"" + name + "\".");
		}
	}

	private static UUID requireWorkOrder(UUID workOrderId) {
		return Guard.requireNonNull(workOrderId, "A ordem de serviço da movimentação é obrigatória.");
	}

	private void changeData(String name, BigDecimal minimumStock, Instant now) {
		this.name = Guard.requireText(name, "O nome da peça é obrigatório.");
		Guard.requireNonNull(minimumStock, "O estoque mínimo é obrigatório.");
		if (minimumStock.signum() < 0) {
			throw new InvalidValueException("O estoque mínimo não pode ser negativo.");
		}
		if (!unitOfMeasure.allowsFraction() && minimumStock.stripTrailingZeros().scale() > 0) {
			throw new InvalidValueException("O estoque mínimo de itens em UN deve ser inteiro.");
		}
		this.minimumStock = minimumStock;
		this.updatedAt = Objects.requireNonNull(now);
	}

	public PartId getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public PartType getType() {
		return type;
	}

	public UnitOfMeasure getUnitOfMeasure() {
		return unitOfMeasure;
	}

	public Money getUnitPrice() {
		return unitPrice;
	}

	public BigDecimal getAvailableQuantity() {
		return availableQuantity;
	}

	public BigDecimal getReservedQuantity() {
		return reservedQuantity;
	}

	public BigDecimal getMinimumStock() {
		return minimumStock;
	}

	public boolean isActive() {
		return active;
	}

	public Long getVersion() {
		return version;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
