package br.dev.lucasaguiar.mecano.workorder.domain;

import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.shared.domain.Guard;
import br.dev.lucasaguiar.mecano.shared.domain.InvalidValueException;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import br.dev.lucasaguiar.mecano.shared.domain.Quantity;
import br.dev.lucasaguiar.mecano.shared.domain.UnitOfMeasure;
import java.util.Objects;
import java.util.UUID;

/**
 * Peça ou insumo incluído na OS, com descrição, unidade e preço copiados no momento da inclusão.
 */
public class PartItem {

	private final UUID id;
	private final PartId partId;
	private final String description;
	private final UnitOfMeasure unitOfMeasure;
	private final Money unitPrice;
	private final Quantity quantity;

	private PartItem(UUID id, PartId partId, String description, UnitOfMeasure unitOfMeasure, Money unitPrice,
			Quantity quantity) {
		this.id = Objects.requireNonNull(id);
		this.partId = Guard.requireNonNull(partId, "A peça é obrigatória.");
		this.description = Guard.requireText(description, "A descrição da peça é obrigatória.");
		this.unitOfMeasure = Guard.requireNonNull(unitOfMeasure, "A unidade de medida é obrigatória.");
		this.unitPrice = Guard.requireNonNull(unitPrice, "O preço da peça é obrigatório.");
		this.quantity = Guard.requireNonNull(quantity, "A quantidade é obrigatória.");
		if (!unitOfMeasure.allowsFraction() && !quantity.isWhole()) {
			throw new InvalidValueException("\"" + description + "\" é controlado em " + unitOfMeasure
					+ " e não aceita quantidade fracionada.");
		}
	}

	static PartItem create(PartId partId, String description, UnitOfMeasure unitOfMeasure, Money unitPrice,
			Quantity quantity) {
		return new PartItem(UUID.randomUUID(), partId, description, unitOfMeasure, unitPrice, quantity);
	}

	public static PartItem restore(UUID id, PartId partId, String description, UnitOfMeasure unitOfMeasure,
			Money unitPrice, Quantity quantity) {
		return new PartItem(id, partId, description, unitOfMeasure, unitPrice, quantity);
	}

	public Money subtotal() {
		return unitPrice.multiply(quantity);
	}

	public UUID getId() {
		return id;
	}

	public PartId getPartId() {
		return partId;
	}

	public String getDescription() {
		return description;
	}

	public UnitOfMeasure getUnitOfMeasure() {
		return unitOfMeasure;
	}

	public Money getUnitPrice() {
		return unitPrice;
	}

	public Quantity getQuantity() {
		return quantity;
	}
}
