package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerRepository;
import br.dev.lucasaguiar.mecano.shared.application.NotFoundException;
import br.dev.lucasaguiar.mecano.shared.domain.Page;
import br.dev.lucasaguiar.mecano.shared.domain.PageQuery;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista o histórico de OS de um cliente, das mais recentes para as mais antigas.
 */
@Service
public class ListCustomerWorkOrdersUseCase {

	private final WorkOrderRepository workOrderRepository;
	private final CustomerRepository customerRepository;

	public ListCustomerWorkOrdersUseCase(WorkOrderRepository workOrderRepository,
			CustomerRepository customerRepository) {
		this.workOrderRepository = workOrderRepository;
		this.customerRepository = customerRepository;
	}

	@Transactional(readOnly = true)
	public Page<WorkOrder> execute(CustomerId customerId, PageQuery query) {
		customerRepository.findById(customerId).orElseThrow(() -> new NotFoundException("Cliente não encontrado."));
		return workOrderRepository.findByCustomer(customerId, query);
	}
}
