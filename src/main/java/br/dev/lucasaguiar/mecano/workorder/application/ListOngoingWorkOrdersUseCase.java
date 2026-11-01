package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.shared.domain.Page;
import br.dev.lucasaguiar.mecano.shared.domain.PageQuery;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista a fila de OS em andamento da oficina: em execução, aguardando aprovação, em diagnóstico e recebidas, nessa
 * ordem e das mais antigas para as mais recentes dentro de cada status. Finalizadas e entregues ficam de fora.
 */
@Service
public class ListOngoingWorkOrdersUseCase {

	private final WorkOrderRepository workOrderRepository;

	public ListOngoingWorkOrdersUseCase(WorkOrderRepository workOrderRepository) {
		this.workOrderRepository = workOrderRepository;
	}

	@Transactional(readOnly = true)
	public Page<WorkOrder> execute(PageQuery query) {
		return workOrderRepository.findOngoing(query);
	}
}
