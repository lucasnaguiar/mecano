package br.dev.lucasaguiar.mecano.catalog.domain;

import br.dev.lucasaguiar.mecano.shared.domain.AggregateRoot;
import br.dev.lucasaguiar.mecano.shared.domain.BusinessRuleException;
import br.dev.lucasaguiar.mecano.shared.domain.Guard;
import br.dev.lucasaguiar.mecano.shared.domain.InvalidValueException;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root: serviço oferecido no catálogo da oficina, com preço e tempo estimado.
 * Alterar o preço não afeta ordens de serviço já orçadas, que guardam uma cópia.
 */
public class LaborOperation extends AggregateRoot {

	private final LaborOperationId id;
	private final Instant createdAt;
	private String name;
	private String description;
	private Money price;
	private Duration estimatedDuration;
	private boolean active;
	private Instant updatedAt;

	private LaborOperation(LaborOperationId id, String name, String description, Money price,
			Duration estimatedDuration, boolean active, Instant createdAt, Instant updatedAt) {
		this.id = Objects.requireNonNull(id);
		this.createdAt = Objects.requireNonNull(createdAt);
		this.active = active;
		changeData(name, description, price, estimatedDuration, updatedAt);
	}

	public static LaborOperation register(String name, String description, Money price, Duration estimatedDuration,
			Instant now) {
		return new LaborOperation(LaborOperationId.newId(), name, description, price, estimatedDuration, true, now, now);
	}

	public static LaborOperation restore(LaborOperationId id, String name, String description, Money price,
			Duration estimatedDuration, boolean active, Instant createdAt, Instant updatedAt) {
		return new LaborOperation(id, name, description, price, estimatedDuration, active, createdAt, updatedAt);
	}

	public void update(String name, String description, Money price, Duration estimatedDuration, Instant now) {
		changeData(name, description, price, estimatedDuration, now);
	}

	public void deactivate(Instant now) {
		active = false;
		updatedAt = now;
	}

	public void activate(Instant now) {
		active = true;
		updatedAt = now;
	}

	public void ensureActive() {
		if (!active) {
			throw new BusinessRuleException("O serviço \"" + name + "\" está inativo e não pode ser incluído.");
		}
	}

	private void changeData(String name, String description, Money price, Duration estimatedDuration, Instant now) {
		this.name = Guard.requireText(name, "O nome do serviço é obrigatório.");
		this.description = Guard.optionalText(description);
		this.price = Guard.requireNonNull(price, "O preço do serviço é obrigatório.");
		Guard.requireNonNull(estimatedDuration, "O tempo estimado é obrigatório.");
		if (estimatedDuration.isNegative() || estimatedDuration.toMinutes() < 1) {
			throw new InvalidValueException("O tempo estimado deve ser de pelo menos 1 minuto.");
		}
		this.estimatedDuration = estimatedDuration;
		this.updatedAt = Objects.requireNonNull(now);
	}

	public LaborOperationId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Money getPrice() {
		return price;
	}

	public Duration getEstimatedDuration() {
		return estimatedDuration;
	}

	public boolean isActive() {
		return active;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
