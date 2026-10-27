package br.dev.lucasaguiar.mecano.customer.domain.vehicle.event;

import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleId;
import java.time.Instant;

/**
 * Evento de domínio: veículo cadastrado.
 */
public record VehicleRegistered(VehicleId vehicleId, Instant occurredAt) implements DomainEvent {
}
