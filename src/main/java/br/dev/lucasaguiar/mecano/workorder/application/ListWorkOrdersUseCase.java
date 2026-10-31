package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderRepository;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderStatus;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista as OS, opcionalmente filtradas por status, das mais recentes para as mais antigas.
 */
@Service
public class ListWorkOrdersUseCase {

	private final WorkOrderRepository workOrderRepository;

	public ListWorkOrdersUseCase(WorkOrderRepository workOrderRepository) {
		this.workOrderRepository = workOrderRepository;
	}

	@Transactional(readOnly = true)
	public List<WorkOrder> execute(WorkOrderStatus status) {
		return workOrderRepository.findAll(status);
	}
}
