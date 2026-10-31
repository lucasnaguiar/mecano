package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.shared.application.DomainEventPublisher;
import br.dev.lucasaguiar.mecano.shared.application.NotFoundException;
import br.dev.lucasaguiar.mecano.workorder.domain.TrackingCode;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderRepository;
import org.springframework.stereotype.Component;

/**
 * Carrega e salva a OS para os casos de uso. Os eventos são publicados depois de salvar, na mesma transação, para
 * que as políticas (ex.: reserva de estoque) possam desfazer a operação inteira em caso de falha.
 */
@Component
class WorkOrderStore {

	private final WorkOrderRepository workOrderRepository;
	private final DomainEventPublisher eventPublisher;

	WorkOrderStore(WorkOrderRepository workOrderRepository, DomainEventPublisher eventPublisher) {
		this.workOrderRepository = workOrderRepository;
		this.eventPublisher = eventPublisher;
	}

	WorkOrder load(WorkOrderId id) {
		return workOrderRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada."));
	}

	WorkOrder load(TrackingCode trackingCode) {
		return workOrderRepository.findByTrackingCode(trackingCode)
				.orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada."));
	}

	WorkOrder save(WorkOrder workOrder) {
		var saved = workOrderRepository.save(workOrder);
		eventPublisher.publish(workOrder.pullEvents());
		return saved;
	}
}
