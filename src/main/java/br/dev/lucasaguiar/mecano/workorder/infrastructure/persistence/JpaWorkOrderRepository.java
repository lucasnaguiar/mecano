package br.dev.lucasaguiar.mecano.workorder.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationId;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleId;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import br.dev.lucasaguiar.mecano.shared.domain.Page;
import br.dev.lucasaguiar.mecano.shared.domain.PageQuery;
import br.dev.lucasaguiar.mecano.shared.domain.Quantity;
import br.dev.lucasaguiar.mecano.shared.domain.UnitOfMeasure;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import br.dev.lucasaguiar.mecano.shared.infrastructure.persistence.Pages;
import br.dev.lucasaguiar.mecano.workorder.domain.Estimate;
import br.dev.lucasaguiar.mecano.workorder.domain.LaborItem;
import br.dev.lucasaguiar.mecano.workorder.domain.PartItem;
import br.dev.lucasaguiar.mecano.workorder.domain.StatusTransition;
import br.dev.lucasaguiar.mecano.workorder.domain.TrackingCode;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderRepository;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderStatus;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/**
 * Implementação JPA de WorkOrderRepository. Grava o agregado inteiro (itens, orçamentos e transições) em cascata e
 * usa saveAndFlush para obter o número gerado pela sequence e acusar conflitos de versão dentro do caso de uso.
 */
@Repository
public class JpaWorkOrderRepository implements WorkOrderRepository {

	private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id"));

	private final SpringDataWorkOrderRepository springDataRepository;

	public JpaWorkOrderRepository(SpringDataWorkOrderRepository springDataRepository) {
		this.springDataRepository = springDataRepository;
	}

	@Override
	public WorkOrder save(WorkOrder workOrder) {
		return toDomain(springDataRepository.saveAndFlush(toEntity(workOrder)));
	}

	@Override
	public Optional<WorkOrder> findById(WorkOrderId id) {
		return springDataRepository.findById(id.value()).map(JpaWorkOrderRepository::toDomain);
	}

	@Override
	public Optional<WorkOrder> findByTrackingCode(TrackingCode trackingCode) {
		return springDataRepository.findByTrackingCode(trackingCode.value()).map(JpaWorkOrderRepository::toDomain);
	}

	@Override
	public Page<WorkOrder> findAll(WorkOrderStatus status, PageQuery query) {
		var pageable = Pages.toPageable(query, NEWEST_FIRST);
		var entities = status == null
				? springDataRepository.findAll(pageable)
				: springDataRepository.findByStatus(status, pageable);
		return Pages.toPage(entities, JpaWorkOrderRepository::toDomain);
	}

	@Override
	public Page<WorkOrder> findByCustomer(CustomerId customerId, PageQuery query) {
		return Pages.toPage(springDataRepository.findByCustomerId(customerId.value(),
				Pages.toPageable(query, NEWEST_FIRST)), JpaWorkOrderRepository::toDomain);
	}

	@Override
	public Page<WorkOrder> findByVehicle(VehicleId vehicleId, PageQuery query) {
		return Pages.toPage(springDataRepository.findByVehicleId(vehicleId.value(),
				Pages.toPageable(query, NEWEST_FIRST)), JpaWorkOrderRepository::toDomain);
	}

	@Override
	public Page<WorkOrder> findOngoing(PageQuery query) {
		return Pages.toPage(springDataRepository.findOngoing(Pages.toPageable(query)), JpaWorkOrderRepository::toDomain);
	}

	@Override
	public boolean hasOpenWorkOrder(VehicleId vehicleId) {
		return springDataRepository.existsByVehicleIdAndStatusNot(vehicleId.value(), WorkOrderStatus.DELIVERED);
	}

	@Override
	public Optional<Duration> averageExecutionTime() {
		return Optional.ofNullable(springDataRepository.findAverageExecutionTimeInSeconds())
				.map(seconds -> Duration.ofSeconds(Math.round(seconds)));
	}

	private static WorkOrderEntity toEntity(WorkOrder workOrder) {
		return new WorkOrderEntity(workOrder.getId().value(), workOrder.getNumber(),
				workOrder.getTrackingCode().value(), workOrder.getCustomerId().value(),
				workOrder.getVehicleId().value(), workOrder.getStatus(), workOrder.getProblemDescription(),
				workOrder.getDiagnosisNotes(), workOrder.getTotalAmount().amount(), workOrder.getVersion(),
				workOrder.getCreatedAt(), workOrder.getUpdatedAt(),
				map(workOrder.getLaborItems(), item -> new LaborItemEntity(item.getId(),
						item.getLaborOperationId().value(), item.getDescription(), item.getUnitPrice().amount(),
						item.getQuantity())),
				map(workOrder.getPartItems(), item -> new PartItemEntity(item.getId(), item.getPartId().value(),
						item.getDescription(), item.getUnitOfMeasure().name(), item.getUnitPrice().amount(),
						item.getQuantity().value())),
				map(workOrder.getEstimates(), estimate -> new EstimateEntity(estimate.getId(), estimate.getStatus(),
						estimate.getTotalAmount().amount(), estimate.getSentAt(), estimate.getRespondedAt())),
				map(workOrder.getStatusTransitions(), transition -> new StatusTransitionEntity(transition.getId(),
						transition.getFromStatus(), transition.getToStatus(),
						transition.getChangedBy() == null ? null : transition.getChangedBy().value(),
						transition.getOccurredAt())));
	}

	private static WorkOrder toDomain(WorkOrderEntity entity) {
		return WorkOrder.restore(new WorkOrderId(entity.getId()), entity.getNumber(),
				new TrackingCode(entity.getTrackingCode()), new CustomerId(entity.getCustomerId()),
				new VehicleId(entity.getVehicleId()), entity.getStatus(), entity.getProblemDescription(),
				entity.getDiagnosisNotes(), entity.getVersion(), entity.getCreatedAt(), entity.getUpdatedAt(),
				map(entity.getLaborItems(), item -> LaborItem.restore(item.getId(),
						new LaborOperationId(item.getLaborOperationId()), item.getDescription(),
						new Money(item.getUnitPrice()), item.getQuantity())),
				map(entity.getPartItems(), item -> PartItem.restore(item.getId(), new PartId(item.getPartId()),
						item.getDescription(), UnitOfMeasure.valueOf(item.getUnitOfMeasure()),
						new Money(item.getUnitPrice()), new Quantity(item.getQuantity()))),
				map(entity.getEstimates(), estimate -> Estimate.restore(estimate.getId(), estimate.getStatus(),
						new Money(estimate.getTotalAmount()), estimate.getSentAt(), estimate.getRespondedAt())),
				map(entity.getStatusTransitions(), transition -> StatusTransition.restore(transition.getId(),
						transition.getFromStatus(), transition.getToStatus(),
						transition.getChangedBy() == null ? null : new UserId(transition.getChangedBy()),
						transition.getOccurredAt())));
	}

	private static <S, T> List<T> map(List<S> source, Function<S, T> mapper) {
		return new ArrayList<>(source.stream().map(mapper).toList());
	}
}
