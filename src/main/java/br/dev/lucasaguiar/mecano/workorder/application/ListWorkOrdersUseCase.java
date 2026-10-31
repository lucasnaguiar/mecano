package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.shared.domain.Page;
import br.dev.lucasaguiar.mecano.shared.domain.PageQuery;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderRepository;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista as OS, opcionalmente filtradas por status, das mais recentes para as mais antigas, paginadas.
 */
@Service
public class ListWorkOrdersUseCase {

	private final WorkOrderRepository workOrderRepository;

	public ListWorkOrdersUseCase(WorkOrderRepository workOrderRepository) {
		this.workOrderRepository = workOrderRepository;
	}

	@Transactional(readOnly = true)
	public Page<WorkOrder> execute(WorkOrderStatus status, PageQuery query) {
		return workOrderRepository.findAll(status, query);
	}
}
