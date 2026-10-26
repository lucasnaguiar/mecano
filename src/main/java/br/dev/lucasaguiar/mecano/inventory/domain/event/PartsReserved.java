package br.dev.lucasaguiar.mecano.inventory.domain.event;

import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import java.time.Instant;

/**
 * Evento de domínio: Peças reservadas para uma ordem de serviço.
 */
public record PartsReserved(PartId partId, Instant occurredAt) implements DomainEvent {
}
