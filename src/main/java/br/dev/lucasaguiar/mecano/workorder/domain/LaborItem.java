package br.dev.lucasaguiar.mecano.workorder.domain;

import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationId;
import br.dev.lucasaguiar.mecano.shared.domain.Guard;
import br.dev.lucasaguiar.mecano.shared.domain.InvalidValueException;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import java.util.Objects;
import java.util.UUID;

/**
 * Serviço do catálogo incluído na OS, com descrição e preço copiados no momento da inclusão.
 */
public class LaborItem {

	private final UUID id;
	private final LaborOperationId laborOperationId;
	private final String description;
	private final Money unitPrice;
	private final int quantity;

	private LaborItem(UUID id, LaborOperationId laborOperationId, String description, Money unitPrice, int quantity) {
		this.id = Objects.requireNonNull(id);
		this.laborOperationId = Guard.requireNonNull(laborOperationId, "O serviço do catálogo é obrigatório.");
		this.description = Guard.requireText(description, "A descrição do serviço é obrigatória.");
		this.unitPrice = Guard.requireNonNull(unitPrice, "O preço do serviço é obrigatório.");
		if (quantity < 1) {
			throw new InvalidValueException("A quantidade do serviço deve ser maior que zero.");
		}
		this.quantity = quantity;
	}

	static LaborItem create(LaborOperationId laborOperationId, String description, Money unitPrice, int quantity) {
		return new LaborItem(UUID.randomUUID(), laborOperationId, description, unitPrice, quantity);
	}

	public static LaborItem restore(UUID id, LaborOperationId laborOperationId, String description, Money unitPrice,
			int quantity) {
		return new LaborItem(id, laborOperationId, description, unitPrice, quantity);
	}

	public Money subtotal() {
		return unitPrice.multiply(quantity);
	}

	public UUID getId() {
		return id;
	}

	public LaborOperationId getLaborOperationId() {
		return laborOperationId;
	}

	public String getDescription() {
		return description;
	}

	public Money getUnitPrice() {
		return unitPrice;
	}

	public int getQuantity() {
		return quantity;
	}
}
