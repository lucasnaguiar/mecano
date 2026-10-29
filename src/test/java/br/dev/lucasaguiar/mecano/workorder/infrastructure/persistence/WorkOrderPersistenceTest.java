package br.dev.lucasaguiar.mecano.workorder.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import br.dev.lucasaguiar.mecano.PersistenceTest;
import br.dev.lucasaguiar.mecano.workorder.domain.EstimateStatus;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderStatus;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;

@PersistenceTest
class WorkOrderPersistenceTest {

	private static final Instant NOW = Instant.parse("2026-01-15T12:00:00Z");

	@Autowired
	private SpringDataWorkOrderRepository workOrders;

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbc;

	private UUID customerId;
	private UUID vehicleId;
	private UUID laborOperationId;
	private UUID partId;

	@BeforeEach
	void insertReferencedRows() {
		customerId = UUID.randomUUID();
		vehicleId = UUID.randomUUID();
		laborOperationId = UUID.randomUUID();
		partId = UUID.randomUUID();
		jdbc.update("INSERT INTO customer VALUES (?, 'Ana', ?, NULL, NULL, now(), now())",
				customerId, randomDigits(11));
		jdbc.update("INSERT INTO vehicle VALUES (?, ?, ?, 'Fiat', 'Uno', 2015, now(), now())",
				vehicleId, customerId, "ABC" + randomDigits(4));
		jdbc.update("INSERT INTO labor_operation VALUES (?, ?, NULL, 120.00, 60, TRUE, now(), now())",
				laborOperationId, "Troca de óleo " + laborOperationId);
		jdbc.update("INSERT INTO part VALUES (?, ?, 'Óleo 5W30', 'SUPPLY', 'L', 45.90, 10, 0, 2, TRUE, 0, now(), now())",
				partId, "OLEO-" + partId);
	}

	@Test
	void savesAggregateWithChildrenAndGeneratesNumber() {
		var workOrder = workOrder(WorkOrderStatus.AWAITING_APPROVAL);
		workOrder.getLaborItems().add(new LaborItemEntity(UUID.randomUUID(), laborOperationId, "Troca de óleo",
				new BigDecimal("120.00"), 1));
		workOrder.getPartItems().add(new PartItemEntity(UUID.randomUUID(), partId, "Óleo 5W30", "L",
				new BigDecimal("45.90"), new BigDecimal("3.750")));
		workOrder.getEstimates().add(new EstimateEntity(UUID.randomUUID(), EstimateStatus.PENDING,
				new BigDecimal("292.13"), NOW, null));
		workOrder.getStatusTransitions().add(transition(null, WorkOrderStatus.RECEIVED, NOW));

		var saved = workOrders.saveAndFlush(workOrder);
		entityManager.clear();

		var loaded = workOrders.findByTrackingCode(saved.getTrackingCode()).orElseThrow();
		assertThat(loaded.getNumber()).isGreaterThanOrEqualTo(1000L);
		assertThat(workOrders.findByNumber(loaded.getNumber())).isPresent();
		assertThat(loaded.getLaborItems()).hasSize(1);
		assertThat(loaded.getPartItems()).singleElement()
				.extracting(PartItemEntity::getQuantity).isEqualTo(new BigDecimal("3.750"));
		assertThat(loaded.getEstimates()).singleElement()
				.extracting(EstimateEntity::getStatus).isEqualTo(EstimateStatus.PENDING);
		assertThat(loaded.getStatusTransitions()).hasSize(1);
	}

	@Test
	void removesChildrenWhenTheyLeaveTheAggregate() {
		var workOrder = workOrder(WorkOrderStatus.IN_DIAGNOSIS);
		workOrder.getLaborItems().add(new LaborItemEntity(UUID.randomUUID(), laborOperationId, "Troca de óleo",
				new BigDecimal("120.00"), 1));
		workOrders.saveAndFlush(workOrder);

		var loaded = workOrders.findById(workOrder.getId()).orElseThrow();
		loaded.getLaborItems().clear();
		workOrders.saveAndFlush(loaded);

		assertThat(jdbc.queryForObject("SELECT count(*) FROM labor_item WHERE work_order_id = ?", Long.class,
				workOrder.getId())).isZero();
	}

	@Test
	void calculatesAverageExecutionTimeOfCompletedWorkOrders() {
		saveCompleted(Duration.ofHours(2));
		saveCompleted(Duration.ofHours(4));
		var inProgress = workOrder(WorkOrderStatus.IN_PROGRESS);
		inProgress.getStatusTransitions().add(transition(WorkOrderStatus.AWAITING_APPROVAL, WorkOrderStatus.IN_PROGRESS, NOW));
		workOrders.saveAndFlush(inProgress);

		assertThat(workOrders.findAverageExecutionTimeInSeconds()).isEqualTo(Duration.ofHours(3).toSeconds());
	}

	@Test
	void averageExecutionTimeIsNullWithoutCompletedWorkOrders() {
		assertThat(workOrders.findAverageExecutionTimeInSeconds()).isNull();
	}

	private void saveCompleted(Duration executionTime) {
		var workOrder = workOrder(WorkOrderStatus.COMPLETED);
		workOrder.getStatusTransitions().add(
				transition(WorkOrderStatus.AWAITING_APPROVAL, WorkOrderStatus.IN_PROGRESS, NOW));
		workOrder.getStatusTransitions().add(
				transition(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.COMPLETED, NOW.plus(executionTime)));
		workOrders.saveAndFlush(workOrder);
	}

	private WorkOrderEntity workOrder(WorkOrderStatus status) {
		return new WorkOrderEntity(UUID.randomUUID(), null, UUID.randomUUID().toString().replace("-", ""),
				customerId, vehicleId, status, "Barulho no motor", null, BigDecimal.ZERO, null, NOW, NOW,
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}

	private static StatusTransitionEntity transition(WorkOrderStatus from, WorkOrderStatus to, Instant at) {
		return new StatusTransitionEntity(UUID.randomUUID(), from, to, null, at);
	}

	private static String randomDigits(int length) {
		var digits = new StringBuilder();
		new java.util.Random().ints(length, 0, 10).forEach(digits::append);
		return digits.toString();
	}
}
