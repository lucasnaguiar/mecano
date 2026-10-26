package br.dev.lucasaguiar.mecano.inventory.domain.event;

import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import java.time.Instant;

/**
 * Evento de domínio: Reserva de peças liberada.
 */
public record ReservationReleased(PartId partId, Instant occurredAt) implements DomainEvent {
}
