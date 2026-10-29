package br.dev.lucasaguiar.mecano.shared.infrastructure.event;

import br.dev.lucasaguiar.mecano.shared.application.DomainEventPublisher;
import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publica os eventos pelo ApplicationEventPublisher. Listeners síncronos rodam na mesma transação do caso de uso.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

	private final ApplicationEventPublisher publisher;

	public SpringDomainEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void publish(List<DomainEvent> events) {
		events.forEach(publisher::publishEvent);
	}
}
