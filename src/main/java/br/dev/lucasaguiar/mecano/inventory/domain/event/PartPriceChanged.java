package br.dev.lucasaguiar.mecano.inventory.domain.event;

import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import java.time.Instant;

/**
 * Evento de domínio: preço da peça ou insumo alterado.
 */
public record PartPriceChanged(PartId partId, Instant occurredAt) implements DomainEvent {
}
