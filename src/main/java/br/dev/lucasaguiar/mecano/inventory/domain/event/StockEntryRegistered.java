package br.dev.lucasaguiar.mecano.inventory.domain.event;

import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import java.time.Instant;

/**
 * Evento de domínio: entrada de unidades registrada no estoque.
 */
public record StockEntryRegistered(PartId partId, Instant occurredAt) implements DomainEvent {
}
