package br.dev.lucasaguiar.mecano.workorder.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface SpringDataWorkOrderRepository extends JpaRepository<WorkOrderEntity, UUID> {

	Optional<WorkOrderEntity> findByNumber(Long number);

	Optional<WorkOrderEntity> findByTrackingCode(String trackingCode);

	Page<WorkOrderEntity> findByStatus(WorkOrderStatus status, Pageable pageable);

	Page<WorkOrderEntity> findByCustomerId(UUID customerId, Pageable pageable);

	Page<WorkOrderEntity> findByVehicleId(UUID vehicleId, Pageable pageable);

	/** OS em andamento ordenadas pela prioridade do status e, dentro dele, pela data de abertura. */
	@Query(value = """
			SELECT w FROM WorkOrderEntity w
			WHERE w.status IN (IN_PROGRESS, AWAITING_APPROVAL, IN_DIAGNOSIS, RECEIVED)
			ORDER BY CASE w.status
			  WHEN IN_PROGRESS THEN 0
			  WHEN AWAITING_APPROVAL THEN 1
			  WHEN IN_DIAGNOSIS THEN 2
			  ELSE 3
			END, w.createdAt, w.id
			""", countQuery = """
			SELECT COUNT(w) FROM WorkOrderEntity w
			WHERE w.status IN (IN_PROGRESS, AWAITING_APPROVAL, IN_DIAGNOSIS, RECEIVED)
			""")
	Page<WorkOrderEntity> findOngoing(Pageable pageable);

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
