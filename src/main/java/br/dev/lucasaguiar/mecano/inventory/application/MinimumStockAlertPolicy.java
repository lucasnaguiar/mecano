package br.dev.lucasaguiar.mecano.inventory.application;

import br.dev.lucasaguiar.mecano.inventory.domain.PartRepository;
import br.dev.lucasaguiar.mecano.inventory.domain.event.MinimumStockReached;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Política "Alertar estoque mínimo": registra o alerta para o gestor de estoque. Os itens em alerta também podem ser
 * consultados em GET /api/admin/parts?atOrBelowMinimum=true.
 */
@Component
class MinimumStockAlertPolicy {

	private static final Logger log = LoggerFactory.getLogger(MinimumStockAlertPolicy.class);

	private final PartRepository partRepository;

	MinimumStockAlertPolicy(PartRepository partRepository) {
		this.partRepository = partRepository;
	}

	@EventListener
	void on(MinimumStockReached event) {
		partRepository.findById(event.partId()).ifPresent(part -> log.warn(
				"Estoque mínimo atingido: {} ({}) com {} {} disponíveis (mínimo {}).", part.getName(), part.getCode(),
				part.getAvailableQuantity().stripTrailingZeros().toPlainString(), part.getUnitOfMeasure(),
				part.getMinimumStock().stripTrailingZeros().toPlainString()));
	}
}
