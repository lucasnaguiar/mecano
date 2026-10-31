package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleRepository;
import br.dev.lucasaguiar.mecano.shared.application.NotFoundException;
import br.dev.lucasaguiar.mecano.shared.domain.Page;
import br.dev.lucasaguiar.mecano.shared.domain.PageQuery;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista o histórico de OS de um veículo, das mais recentes para as mais antigas.
 */
@Service
public class ListVehicleWorkOrdersUseCase {

	private final WorkOrderRepository workOrderRepository;
	private final VehicleRepository vehicleRepository;

	public ListVehicleWorkOrdersUseCase(WorkOrderRepository workOrderRepository, VehicleRepository vehicleRepository) {
		this.workOrderRepository = workOrderRepository;
		this.vehicleRepository = vehicleRepository;
	}

	@Transactional(readOnly = true)
	public Page<WorkOrder> execute(VehicleId vehicleId, PageQuery query) {
		vehicleRepository.findById(vehicleId).orElseThrow(() -> new NotFoundException("Veículo não encontrado."));
		return workOrderRepository.findByVehicle(vehicleId, query);
	}
}
