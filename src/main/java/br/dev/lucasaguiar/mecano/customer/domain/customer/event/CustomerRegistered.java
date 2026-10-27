package br.dev.lucasaguiar.mecano.customer.domain.customer.event;

import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import java.time.Instant;

/**
 * Evento de domínio: cliente cadastrado.
 */
public record CustomerRegistered(CustomerId customerId, Instant occurredAt) implements DomainEvent {
}
