package br.dev.lucasaguiar.mecano.shared.application;

import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import java.util.List;

/**
 * Publica os eventos de domínio gerados por um agregado depois de salvo.
 */
public interface DomainEventPublisher {

	void publish(List<DomainEvent> events);
}
