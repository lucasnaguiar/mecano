package br.dev.lucasaguiar.mecano.workorder.domain;

import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleId;
import br.dev.lucasaguiar.mecano.shared.domain.Page;
import br.dev.lucasaguiar.mecano.shared.domain.PageQuery;
import java.time.Duration;
import java.util.Optional;

/**
 * Repositório do agregado WorkOrder.
 */
public interface WorkOrderRepository {

	WorkOrder save(WorkOrder workOrder);

	Optional<WorkOrder> findById(WorkOrderId id);

	Optional<WorkOrder> findByTrackingCode(TrackingCode trackingCode);

	/** Lista as OS do status informado, ou todas quando nulo, das mais recentes para as mais antigas. */
	Page<WorkOrder> findAll(WorkOrderStatus status, PageQuery query);

	/** Lista as OS do cliente, das mais recentes para as mais antigas. */
	Page<WorkOrder> findByCustomer(CustomerId customerId, PageQuery query);

	/** Lista as OS do veículo, das mais recentes para as mais antigas. */
	Page<WorkOrder> findByVehicle(VehicleId vehicleId, PageQuery query);

	/** Indica se o veículo já tem uma OS ainda não entregue. */
	boolean hasOpenWorkOrder(VehicleId vehicleId);

	/** Tempo médio entre o início da execução e a finalização das OS finalizadas. */
	Optional<Duration> averageExecutionTime();
}
