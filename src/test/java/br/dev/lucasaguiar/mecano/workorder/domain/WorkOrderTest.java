package br.dev.lucasaguiar.mecano.workorder.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationId;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleId;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.shared.domain.BusinessRuleException;
import br.dev.lucasaguiar.mecano.shared.domain.DomainEvent;
import br.dev.lucasaguiar.mecano.shared.domain.InvalidValueException;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import br.dev.lucasaguiar.mecano.shared.domain.Quantity;
import br.dev.lucasaguiar.mecano.shared.domain.UnitOfMeasure;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import br.dev.lucasaguiar.mecano.workorder.domain.event.DiagnosisStarted;
import br.dev.lucasaguiar.mecano.workorder.domain.event.EstimateApproved;
import br.dev.lucasaguiar.mecano.workorder.domain.event.EstimateRejected;
import br.dev.lucasaguiar.mecano.workorder.domain.event.EstimateSent;
import br.dev.lucasaguiar.mecano.workorder.domain.event.ExecutionStarted;
import br.dev.lucasaguiar.mecano.workorder.domain.event.LaborAdded;
import br.dev.lucasaguiar.mecano.workorder.domain.event.PartAdded;
import br.dev.lucasaguiar.mecano.workorder.domain.event.RequiredPart;
import br.dev.lucasaguiar.mecano.workorder.domain.event.VehicleDelivered;
import br.dev.lucasaguiar.mecano.workorder.domain.event.WorkOrderCanceled;
import br.dev.lucasaguiar.mecano.workorder.domain.event.WorkOrderCompleted;
import br.dev.lucasaguiar.mecano.workorder.domain.event.WorkOrderOpened;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkOrderTest {

	private static final Instant NOW = Instant.parse("2026-01-15T12:00:00Z");
	private static final UserId ATTENDANT = UserId.newId();
	private static final UserId MECHANIC = UserId.newId();
	private static final LaborOperationId OIL_CHANGE = LaborOperationId.newId();
	private static final PartId FILTER = PartId.newId();
	private static final PartId OIL = PartId.newId();

	private WorkOrder workOrder;

	@BeforeEach
	void setUp() {
		workOrder = WorkOrder.open(CustomerId.newId(), VehicleId.newId(), "Barulho no motor", ATTENDANT, NOW);
	}

	@Test
	void openStartsAsReceivedWithTrackingCodeAndHistory() {
		assertThat(workOrder.getStatus()).isEqualTo(WorkOrderStatus.RECEIVED);
		assertThat(workOrder.getTrackingCode().value()).hasSize(12);
		assertThat(workOrder.getNumber()).isNull();
		assertThat(workOrder.getTotalAmount()).isEqualTo(Money.ZERO);
		assertThat(workOrder.currentEstimate()).isEmpty();
		assertThat(workOrder.getStatusTransitions()).singleElement().satisfies(transition -> {
			assertThat(transition.getFromStatus()).isNull();
			assertThat(transition.getToStatus()).isEqualTo(WorkOrderStatus.RECEIVED);
			assertThat(transition.getChangedBy()).isEqualTo(ATTENDANT);
		});
		assertThat(workOrder.pullEvents()).singleElement().isInstanceOf(WorkOrderOpened.class);
	}

	@Test
	void openRequiresProblemDescription() {
		assertThatThrownBy(() -> WorkOrder.open(CustomerId.newId(), VehicleId.newId(), " ", ATTENDANT, NOW))
				.isInstanceOf(InvalidValueException.class)
				.hasMessage("A descrição do problema é obrigatória.");
	}

	@Test
	void itemsCanOnlyBeAddedDuringDiagnosis() {
		assertThatThrownBy(() -> addOilChange())
				.isInstanceOf(BusinessRuleException.class)
				.hasMessage("Não é possível incluir serviços ou peças: a ordem de serviço está \"Recebida\" e "
						+ "precisa estar \"Em diagnóstico\".");
	}

	@Test
	void addingItemsRecalculatesTotal() {
		workOrder.startDiagnosis(MECHANIC, NOW);

		workOrder.addLabor(OIL_CHANGE, "Troca de óleo", Money.of("120.00"), 1, NOW);
		workOrder.addPart(FILTER, "Filtro de óleo", UnitOfMeasure.UN, Money.of("35.00"), Quantity.of(2), NOW);
		workOrder.addPart(OIL, "Óleo 5W30", UnitOfMeasure.L, Money.of("45.90"), Quantity.of("3.5"), NOW);

		assertThat(workOrder.getTotalAmount()).isEqualTo(Money.of("350.65"));
		assertThat(workOrder.pullEvents()).extracting(DomainEvent::getClass).containsExactly(WorkOrderOpened.class,
				DiagnosisStarted.class, LaborAdded.class, PartAdded.class, PartAdded.class);
	}

	@Test
	void removingItemsRecalculatesTotal() {
		workOrder.startDiagnosis(MECHANIC, NOW);
		var labor = addOilChange();
		var part = workOrder.addPart(FILTER, "Filtro", UnitOfMeasure.UN, Money.of("35"), Quantity.of(1), NOW);

		workOrder.removeLabor(labor.getId(), NOW);
		assertThat(workOrder.getTotalAmount()).isEqualTo(Money.of("35.00"));
		workOrder.removePart(part.getId(), NOW);
		assertThat(workOrder.getTotalAmount()).isEqualTo(Money.ZERO);

		assertThatThrownBy(() -> workOrder.removeLabor(UUID.randomUUID(), NOW))
				.isInstanceOf(BusinessRuleException.class);
		assertThatThrownBy(() -> workOrder.removePart(UUID.randomUUID(), NOW))
				.isInstanceOf(BusinessRuleException.class);
	}

	@Test
	void partItemsInUnitsRejectFractions() {
		workOrder.startDiagnosis(MECHANIC, NOW);

		assertThatThrownBy(() -> workOrder.addPart(FILTER, "Filtro", UnitOfMeasure.UN, Money.of("35"),
				Quantity.of("1.5"), NOW))
				.isInstanceOf(InvalidValueException.class);
		assertThatThrownBy(() -> workOrder.addLabor(OIL_CHANGE, "Troca", Money.of("1"), 0, NOW))
				.isInstanceOf(InvalidValueException.class);
	}

	@Test
	void estimateRequiresItems() {
		workOrder.startDiagnosis(MECHANIC, NOW);

		assertThatThrownBy(() -> workOrder.sendEstimate(null, ATTENDANT, NOW))
				.isInstanceOf(BusinessRuleException.class)
				.hasMessage("Inclua ao menos um serviço ou peça antes de enviar o orçamento.");
	}

	@Test
	void sendEstimateFreezesTotalAndAwaitsApproval() {
		workOrder.startDiagnosis(MECHANIC, NOW);
		addOilChange();

		var estimate = workOrder.sendEstimate(" Óleo vencido ", ATTENDANT, NOW);

		assertThat(workOrder.getStatus()).isEqualTo(WorkOrderStatus.AWAITING_APPROVAL);
		assertThat(workOrder.getDiagnosisNotes()).isEqualTo("Óleo vencido");
		assertThat(estimate.isPending()).isTrue();
		assertThat(estimate.getTotalAmount()).isEqualTo(Money.of("120.00"));
		assertThat(estimate.getSentAt()).isEqualTo(NOW);
		assertThat(workOrder.currentEstimate()).contains(estimate);
		assertThat(workOrder.pullEvents()).last().isInstanceOf(EstimateSent.class);
	}

	@Test
	void approvalKeepsStatusAndRequestsPartsReservation() {
		awaitingApprovalWithParts();

		workOrder.approveEstimate(NOW.plusSeconds(60));

		assertThat(workOrder.getStatus()).isEqualTo(WorkOrderStatus.AWAITING_APPROVAL);
		assertThat(workOrder.currentEstimate()).get().satisfies(estimate -> {
			assertThat(estimate.isApproved()).isTrue();
			assertThat(estimate.getStatus()).isEqualTo(EstimateStatus.APPROVED);
			assertThat(estimate.getRespondedAt()).isEqualTo(NOW.plusSeconds(60));
		});
		var approved = (EstimateApproved) workOrder.pullEvents().getLast();
		assertThat(approved.parts()).containsExactly(new RequiredPart(FILTER, Quantity.of(3)),
				new RequiredPart(OIL, Quantity.of("4")));
	}

	@Test
	void rejectionReturnsToDiagnosisAndAllowsNewEstimate() {
		awaitingApprovalWithParts();

		workOrder.rejectEstimate(NOW);

		assertThat(workOrder.getStatus()).isEqualTo(WorkOrderStatus.IN_DIAGNOSIS);
		assertThat(workOrder.currentEstimate()).get().extracting(Estimate::getStatus)
				.isEqualTo(EstimateStatus.REJECTED);
		assertThat(workOrder.getStatusTransitions().getLast().getChangedBy()).isNull();
		assertThat(workOrder.pullEvents()).last().isInstanceOf(EstimateRejected.class);

		workOrder.sendEstimate(null, ATTENDANT, NOW);
		assertThat(workOrder.getEstimates()).hasSize(2);
		assertThat(workOrder.getDiagnosisNotes()).isEqualTo("Pastilhas gastas");
		assertThat(workOrder.currentEstimate()).get().extracting(Estimate::isPending).isEqualTo(true);
	}

	@Test
	void estimateCanBeAnsweredOnlyOnce() {
		awaitingApprovalWithParts();
		workOrder.approveEstimate(NOW);

		assertThatThrownBy(() -> workOrder.approveEstimate(NOW))
				.isInstanceOf(BusinessRuleException.class)
				.hasMessage("O orçamento desta ordem de serviço já foi respondido.");
		assertThatThrownBy(() -> workOrder.rejectEstimate(NOW)).isInstanceOf(BusinessRuleException.class);
	}

	@Test
	void cannotAnswerEstimateOutsideAwaitingApproval() {
		assertThatThrownBy(() -> workOrder.approveEstimate(NOW))
				.isInstanceOf(BusinessRuleException.class)
				.hasMessage("Não há orçamento aguardando resposta nesta ordem de serviço.");
	}

	@Test
	void executionRequiresApprovedEstimate() {
		awaitingApprovalWithParts();

		assertThatThrownBy(() -> workOrder.startExecution(MECHANIC, NOW))
				.isInstanceOf(BusinessRuleException.class)
				.hasMessage("O orçamento ainda não foi aprovado pelo cliente.");
	}

	@Test
	void fullLifecycleUntilDelivery() {
		awaitingApprovalWithParts();
		workOrder.approveEstimate(NOW.plusSeconds(10));
		workOrder.startExecution(MECHANIC, NOW.plusSeconds(20));
		workOrder.complete(MECHANIC, NOW.plusSeconds(30));
		workOrder.deliver(ATTENDANT, NOW.plusSeconds(40));

		assertThat(workOrder.getStatus()).isEqualTo(WorkOrderStatus.DELIVERED);
		assertThat(workOrder.getUpdatedAt()).isEqualTo(NOW.plusSeconds(40));
		assertThat(workOrder.getStatusTransitions()).extracting(StatusTransition::getToStatus).containsExactly(
				WorkOrderStatus.RECEIVED, WorkOrderStatus.IN_DIAGNOSIS, WorkOrderStatus.AWAITING_APPROVAL,
				WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.COMPLETED, WorkOrderStatus.DELIVERED);
		var events = workOrder.pullEvents();
		assertThat(events).extracting(DomainEvent::getClass).endsWith(EstimateApproved.class,
				ExecutionStarted.class, WorkOrderCompleted.class, VehicleDelivered.class);
		var executionStarted = events.stream().filter(ExecutionStarted.class::isInstance)
				.map(ExecutionStarted.class::cast).findFirst().orElseThrow();
		assertThat(executionStarted.parts()).hasSize(2);
	}

	@Test
	void statusCannotSkipSteps() {
		assertThatThrownBy(() -> workOrder.complete(MECHANIC, NOW)).isInstanceOf(BusinessRuleException.class);
		assertThatThrownBy(() -> workOrder.deliver(ATTENDANT, NOW)).isInstanceOf(BusinessRuleException.class);
		assertThatThrownBy(() -> workOrder.startExecution(MECHANIC, NOW)).isInstanceOf(BusinessRuleException.class);
		workOrder.startDiagnosis(MECHANIC, NOW);
		assertThatThrownBy(() -> workOrder.startDiagnosis(MECHANIC, NOW)).isInstanceOf(BusinessRuleException.class);
	}

	@Test
	void restoreRebuildsAggregateAndTotal() {
		var id = WorkOrderId.newId();
		var labor = LaborItem.restore(UUID.randomUUID(), OIL_CHANGE, "Troca de óleo", Money.of("100"), 2);
		var part = PartItem.restore(UUID.randomUUID(), OIL, "Óleo", UnitOfMeasure.L, Money.of("10"),
				Quantity.of("1.5"));
		var estimate = Estimate.restore(UUID.randomUUID(), EstimateStatus.PENDING, Money.of("215"), NOW, null);
		var transition = StatusTransition.restore(UUID.randomUUID(), null, WorkOrderStatus.RECEIVED, null, NOW);

		var restored = WorkOrder.restore(id, 1000L, new TrackingCode("ABCDEFGHJKLM"), CustomerId.newId(),
				VehicleId.newId(), WorkOrderStatus.AWAITING_APPROVAL, "Problema", "Notas", null, 3L, NOW, NOW,
				List.of(labor), List.of(part), List.of(estimate), List.of(transition));

		assertThat(restored.getId()).isEqualTo(id);
		assertThat(restored.getNumber()).isEqualTo(1000L);
		assertThat(restored.getVersion()).isEqualTo(3L);
		assertThat(restored.getTotalAmount()).isEqualTo(Money.of("215.00"));
		assertThat(restored.getProblemDescription()).isEqualTo("Problema");
		assertThat(restored.getCreatedAt()).isEqualTo(NOW);
		assertThat(restored.getCustomerId()).isNotNull();
		assertThat(restored.getVehicleId()).isNotNull();
		assertThat(labor.subtotal()).isEqualTo(Money.of("200"));
		assertThat(labor.getLaborOperationId()).isEqualTo(OIL_CHANGE);
		assertThat(part.getPartId()).isEqualTo(OIL);
		assertThat(part.getUnitPrice()).isEqualTo(Money.of("10"));
		assertThat(restored.getLaborItems().getFirst().getUnitPrice()).isEqualTo(Money.of("100"));
		assertThat(restored.pullEvents()).isEmpty();
		assertThatThrownBy(() -> restored.getLaborItems().clear()).isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void cancelBeforeDiagnosisRecordsReasonAndHistory() {
		workOrder.pullEvents();

		workOrder.cancel("  Cliente desistiu  ", ATTENDANT, NOW);

		assertThat(workOrder.getStatus()).isEqualTo(WorkOrderStatus.CANCELED);
		assertThat(workOrder.getCancellationReason()).isEqualTo("Cliente desistiu");
		assertThat(workOrder.getStatusTransitions().getLast()).satisfies(transition -> {
			assertThat(transition.getFromStatus()).isEqualTo(WorkOrderStatus.RECEIVED);
			assertThat(transition.getToStatus()).isEqualTo(WorkOrderStatus.CANCELED);
			assertThat(transition.getChangedBy()).isEqualTo(ATTENDANT);
		});
		assertThat(workOrder.pullEvents()).singleElement().isInstanceOfSatisfying(WorkOrderCanceled.class,
				event -> assertThat(event.reservedParts()).isEmpty());
	}

	@Test
	void cancelWithPendingEstimateClosesItWithoutReleasingStock() {
		awaitingApprovalWithParts();
		workOrder.pullEvents();

		workOrder.cancel("Cliente não respondeu", ATTENDANT, NOW);

		assertThat(workOrder.currentEstimate()).get().satisfies(estimate -> {
			assertThat(estimate.getStatus()).isEqualTo(EstimateStatus.CANCELED);
			assertThat(estimate.getRespondedAt()).isEqualTo(NOW);
		});
		assertThat(workOrder.pullEvents()).singleElement().isInstanceOfSatisfying(WorkOrderCanceled.class,
				event -> assertThat(event.reservedParts()).isEmpty());
		assertThatThrownBy(() -> workOrder.approveEstimate(NOW)).isInstanceOf(BusinessRuleException.class)
				.hasMessage("Não há orçamento aguardando resposta nesta ordem de serviço.");
	}

	@Test
	void cancelAfterApprovalReleasesReservedParts() {
		awaitingApprovalWithParts();
		workOrder.approveEstimate(NOW);
		workOrder.pullEvents();

		workOrder.cancel("Cliente levou o veículo", ATTENDANT, NOW);

		assertThat(workOrder.currentEstimate()).get().extracting(Estimate::getStatus)
				.isEqualTo(EstimateStatus.APPROVED);
		assertThat(workOrder.pullEvents()).singleElement().isInstanceOfSatisfying(WorkOrderCanceled.class,
				event -> assertThat(event.reservedParts()).containsExactly(
						new RequiredPart(FILTER, Quantity.of(3)), new RequiredPart(OIL, Quantity.of("4"))));
	}

	@Test
	void cancelAfterRejectionHasNothingToRelease() {
		awaitingApprovalWithParts();
		workOrder.rejectEstimate(NOW);
		workOrder.pullEvents();

		workOrder.cancel("Cliente não quis reorçar", ATTENDANT, NOW);

		assertThat(workOrder.currentEstimate()).get().extracting(Estimate::getStatus)
				.isEqualTo(EstimateStatus.REJECTED);
		assertThat(workOrder.pullEvents()).singleElement().isInstanceOfSatisfying(WorkOrderCanceled.class,
				event -> assertThat(event.reservedParts()).isEmpty());
	}

	@Test
	void cancelRequiresReasonAndIsRefusedOnceExecutionStarted() {
		assertThatThrownBy(() -> workOrder.cancel(" ", ATTENDANT, NOW)).isInstanceOf(InvalidValueException.class)
				.hasMessage("O motivo do cancelamento é obrigatório.");

		awaitingApprovalWithParts();
		workOrder.approveEstimate(NOW);
		workOrder.startExecution(MECHANIC, NOW);

		assertThatThrownBy(() -> workOrder.cancel("Desistência", ATTENDANT, NOW))
				.isInstanceOf(BusinessRuleException.class)
				.hasMessage("Não é possível cancelar: a ordem de serviço está \"Em execução\" e o cancelamento só é "
						+ "permitido antes do início da execução.");
	}

	@Test
	void canceledWorkOrderIsFinal() {
		workOrder.cancel("Duplicada", ATTENDANT, NOW);

		assertThatThrownBy(() -> workOrder.startDiagnosis(MECHANIC, NOW)).isInstanceOf(BusinessRuleException.class);
		assertThatThrownBy(() -> workOrder.cancel("De novo", ATTENDANT, NOW))
				.isInstanceOf(BusinessRuleException.class);
	}

	@Test
	void statusesHavePortugueseLabels() {
		assertThat(WorkOrderStatus.AWAITING_APPROVAL.label()).isEqualTo("Aguardando aprovação");
		assertThat(WorkOrderStatus.CANCELED.label()).isEqualTo("Cancelada");
	}

	private LaborItem addOilChange() {
		return workOrder.addLabor(OIL_CHANGE, "Troca de óleo", Money.of("120.00"), 1, NOW);
	}

	private void awaitingApprovalWithParts() {
		workOrder.startDiagnosis(MECHANIC, NOW);
		addOilChange();
		workOrder.addPart(FILTER, "Filtro", UnitOfMeasure.UN, Money.of("35"), Quantity.of(2), NOW);
		workOrder.addPart(OIL, "Óleo", UnitOfMeasure.L, Money.of("45.90"), Quantity.of("4"), NOW);
		workOrder.addPart(FILTER, "Filtro", UnitOfMeasure.UN, Money.of("35"), Quantity.of(1), NOW);
		workOrder.sendEstimate("Pastilhas gastas", ATTENDANT, NOW);
	}
}
