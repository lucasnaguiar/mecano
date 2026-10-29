package br.dev.lucasaguiar.mecano.shared.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Raiz de agregado: acumula os eventos de domínio gerados pelas operações até que sejam publicados.
 */
public abstract class AggregateRoot {

	private final List<DomainEvent> events = new ArrayList<>();

	protected void registerEvent(DomainEvent event) {
		events.add(event);
	}

	public List<DomainEvent> pullEvents() {
		var pulled = List.copyOf(events);
		events.clear();
		return pulled;
	}
}
