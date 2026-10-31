package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerRepository;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleRepository;
import br.dev.lucasaguiar.mecano.shared.application.ConflictException;
import br.dev.lucasaguiar.mecano.shared.application.NotFoundException;
import br.dev.lucasaguiar.mecano.shared.domain.BusinessRuleException;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abre uma OS para um veículo do cliente. Um veículo não pode ter duas OS em aberto.
 */
@Service
public class OpenWorkOrderUseCase {

	private final WorkOrderStore store;
	private final WorkOrderRepository workOrderRepository;
	private final CustomerRepository customerRepository;
	private final VehicleRepository vehicleRepository;

	public OpenWorkOrderUseCase(WorkOrderStore store, WorkOrderRepository workOrderRepository,
			CustomerRepository customerRepository, VehicleRepository vehicleRepository) {
		this.store = store;
		this.workOrderRepository = workOrderRepository;
		this.customerRepository = customerRepository;
		this.vehicleRepository = vehicleRepository;
	}

	@Transactional
	public WorkOrder execute(CustomerId customerId, VehicleId vehicleId, String problemDescription, UserId openedBy) {
		customerRepository.findById(customerId).orElseThrow(() -> new NotFoundException("Cliente não encontrado."));
		var vehicle = vehicleRepository.findById(vehicleId)
				.orElseThrow(() -> new NotFoundException("Veículo não encontrado."));
		if (!vehicle.belongsTo(customerId)) {
			throw new BusinessRuleException("O veículo informado não pertence ao cliente.");
		}
		if (workOrderRepository.hasOpenWorkOrder(vehicleId)) {
			throw new ConflictException("O veículo já possui uma ordem de serviço em aberto.");
		}
		return store.save(WorkOrder.open(customerId, vehicleId, problemDescription, openedBy, Instant.now()));
	}
}
