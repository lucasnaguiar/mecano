package br.dev.lucasaguiar.mecano.workorder.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface SpringDataWorkOrderRepository extends JpaRepository<WorkOrderEntity, UUID> {

	Optional<WorkOrderEntity> findByNumber(Long number);

	Optional<WorkOrderEntity> findByTrackingCode(String trackingCode);

	List<WorkOrderEntity> findByStatusOrderByCreatedAt(WorkOrderStatus status);

	List<WorkOrderEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

	List<WorkOrderEntity> findByStatusOrderByCreatedAtDesc(WorkOrderStatus status);

	List<WorkOrderEntity> findAllByOrderByCreatedAtDesc();

	boolean existsByVehicleIdAndStatusNot(UUID vehicleId, WorkOrderStatus status);

	/**
	 * Tempo médio, em segundos, entre a entrada em IN_PROGRESS e a entrada em COMPLETED das OS finalizadas.
	 * Retorna nulo quando nenhuma OS foi finalizada.
	 */
	@Query(value = """
			SELECT CAST(AVG(EXTRACT(EPOCH FROM (completed.occurred_at - started.occurred_at))) AS DOUBLE PRECISION)
			FROM status_transition started
			JOIN status_transition completed
			  ON completed.work_order_id = started.work_order_id
			 AND completed.to_status = 'COMPLETED'
			WHERE started.to_status = 'IN_PROGRESS'
			""", nativeQuery = true)
	Double findAverageExecutionTimeInSeconds();
}
