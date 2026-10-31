package br.dev.lucasaguiar.mecano.workorder.domain;

import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationId;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleId;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.shared.domain.AggregateRoot;
import br.dev.lucasaguiar.mecano.shared.domain.BusinessRuleException;
import br.dev.lucasaguiar.mecano.shared.domain.Guard;
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
import br.dev.lucasaguiar.mecano.workorder.domain.event.WorkOrderCompleted;
import br.dev.lucasaguiar.mecano.workorder.domain.event.WorkOrderOpened;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate root: ordem de serviço. Controla o ciclo RECEIVED → IN_DIAGNOSIS → AWAITING_APPROVAL → IN_PROGRESS →
 * COMPLETED → DELIVERED. O status só muda por comandos do negócio, e cada mudança gera um StatusTransition e um
 * evento de domínio. Cliente, veículo, serviços e peças são referenciados apenas pelo ID.
 */
public class WorkOrder extends AggregateRoot {

	private final WorkOrderId id;
	private final Long number;
	private final TrackingCode trackingCode;
	private final CustomerId customerId;
	private final VehicleId vehicleId;
	private final String problemDescription;
	private final Long version;
	private final Instant createdAt;
	private final List<LaborItem> laborItems;
	private final List<PartItem> partItems;
	private final List<Estimate> estimates;
	private final List<StatusTransition> statusTransitions;
	private WorkOrderStatus status;
	private String diagnosisNotes;
	private Money totalAmount;
	private Instant updatedAt;

	private WorkOrder(WorkOrderId id, Long number, TrackingCode trackingCode, CustomerId customerId,
			VehicleId vehicleId, WorkOrderStatus status, String problemDescription, String diagnosisNotes,
			Long version, Instant createdAt, Instant updatedAt, List<LaborItem> laborItems, List<PartItem> partItems,
			List<Estimate> estimates, List<StatusTransition> statusTransitions) {
		this.id = Objects.requireNonNull(id);
		this.number = number;
		this.trackingCode = Objects.requireNonNull(trackingCode);
		this.customerId = Guard.requireNonNull(customerId, "O cliente é obrigatório.");
		this.vehicleId = Guard.requireNonNull(vehicleId, "O veículo é obrigatório.");
		this.status = Objects.requireNonNull(status);
		this.problemDescription = Guard.requireText(problemDescription, "A descrição do problema é obrigatória.");
		this.diagnosisNotes = Guard.optionalText(diagnosisNotes);
		this.version = version;
		this.createdAt = Objects.requireNonNull(createdAt);
		this.updatedAt = Objects.requireNonNull(updatedAt);
		this.laborItems = new ArrayList<>(laborItems);
		this.partItems = new ArrayList<>(partItems);
		this.estimates = new ArrayList<>(estimates);
		this.statusTransitions = new ArrayList<>(statusTransitions);
		this.totalAmount = calculateTotal();
	}

	public static WorkOrder open(CustomerId customerId, VehicleId vehicleId, String problemDescription,
			UserId openedBy, Instant now) {
		var workOrder = new WorkOrder(WorkOrderId.newId(), null, TrackingCode.generate(), customerId, vehicleId,
				WorkOrderStatus.RECEIVED, problemDescription, null, null, now, now, List.of(), List.of(), List.of(),
				List.of());
		workOrder.statusTransitions.add(StatusTransition.record(null, WorkOrderStatus.RECEIVED, openedBy, now));
		workOrder.registerEvent(new WorkOrderOpened(workOrder.id, now));
		return workOrder;
	}

	public static WorkOrder restore(WorkOrderId id, Long number, TrackingCode trackingCode, CustomerId customerId,
			VehicleId vehicleId, WorkOrderStatus status, String problemDescription, String diagnosisNotes,
			Long version, Instant createdAt, Instant updatedAt, List<LaborItem> laborItems, List<PartItem> partItems,
			List<Estimate> estimates, List<StatusTransition> statusTransitions) {
		return new WorkOrder(id, number, trackingCode, customerId, vehicleId, status, problemDescription,
				diagnosisNotes, version, createdAt, updatedAt, laborItems, partItems, estimates, statusTransitions);
	}

	public void startDiagnosis(UserId startedBy, Instant now) {
		requireStatus(WorkOrderStatus.RECEIVED, "iniciar o diagnóstico");
		changeStatus(WorkOrderStatus.IN_DIAGNOSIS, startedBy, now);
		registerEvent(new DiagnosisStarted(id, now));
	}

	public LaborItem addLabor(LaborOperationId laborOperationId, String description, Money unitPrice, int quantity,
			Instant now) {
		requireStatus(WorkOrderStatus.IN_DIAGNOSIS, "incluir serviços ou peças");
		var item = LaborItem.create(laborOperationId, description, unitPrice, quantity);
		laborItems.add(item);
		itemsChanged(now);
		registerEvent(new LaborAdded(id, now));
		return item;
	}

	public PartItem addPart(PartId partId, String description, UnitOfMeasure unitOfMeasure, Money unitPrice,
			Quantity quantity, Instant now) {
		requireStatus(WorkOrderStatus.IN_DIAGNOSIS, "incluir serviços ou peças");
		var item = PartItem.create(partId, description, unitOfMeasure, unitPrice, quantity);
		partItems.add(item);
		itemsChanged(now);
		registerEvent(new PartAdded(id, now));
		return item;
	}

	public void removeLabor(UUID itemId, Instant now) {
		requireStatus(WorkOrderStatus.IN_DIAGNOSIS, "remover serviços ou peças");
		if (!laborItems.removeIf(item -> item.getId().equals(itemId))) {
			throw new BusinessRuleException("O serviço informado não pertence a esta ordem de serviço.");
		}
		itemsChanged(now);
	}

	public void removePart(UUID itemId, Instant now) {
		requireStatus(WorkOrderStatus.IN_DIAGNOSIS, "remover serviços ou peças");
		if (!partItems.removeIf(item -> item.getId().equals(itemId))) {
			throw new BusinessRuleException("A peça informada não pertence a esta ordem de serviço.");
		}
		itemsChanged(now);
	}

	/** Envia o orçamento. Observações em branco mantêm as já registradas (útil ao reorçar). */
	public Estimate sendEstimate(String diagnosisNotes, UserId sentBy, Instant now) {
		requireStatus(WorkOrderStatus.IN_DIAGNOSIS, "enviar o orçamento");
		if (laborItems.isEmpty() && partItems.isEmpty()) {
			throw new BusinessRuleException("Inclua ao menos um serviço ou peça antes de enviar o orçamento.");
		}
		var notes = Guard.optionalText(diagnosisNotes);
		if (notes != null) {
			this.diagnosisNotes = notes;
		}
		var estimate = Estimate.send(totalAmount, now);
		estimates.add(estimate);
		changeStatus(WorkOrderStatus.AWAITING_APPROVAL, sentBy, now);
		registerEvent(new EstimateSent(id, now));
		return estimate;
	}

	/** Aprovação pelo cliente. O status continua AWAITING_APPROVAL até o mecânico iniciar a execução. */
	public void approveEstimate(Instant now) {
		pendingEstimate().approve(now);
		updatedAt = now;
		registerEvent(new EstimateApproved(id, requiredParts(), now));
	}

	/** Recusa pelo cliente: a OS volta ao diagnóstico para ser reorçada. */
	public void rejectEstimate(Instant now) {
		pendingEstimate().reject(now);
		changeStatus(WorkOrderStatus.IN_DIAGNOSIS, null, now);
		registerEvent(new EstimateRejected(id, now));
	}

	public void startExecution(UserId startedBy, Instant now) {
		requireStatus(WorkOrderStatus.AWAITING_APPROVAL, "iniciar a execução");
		if (currentEstimate().filter(Estimate::isApproved).isEmpty()) {
			throw new BusinessRuleException("O orçamento ainda não foi aprovado pelo cliente.");
		}
		changeStatus(WorkOrderStatus.IN_PROGRESS, startedBy, now);
		registerEvent(new ExecutionStarted(id, requiredParts(), now));
	}

	public void complete(UserId completedBy, Instant now) {
		requireStatus(WorkOrderStatus.IN_PROGRESS, "finalizar a ordem de serviço");
		changeStatus(WorkOrderStatus.COMPLETED, completedBy, now);
		registerEvent(new WorkOrderCompleted(id, now));
	}

	public void deliver(UserId deliveredBy, Instant now) {
		requireStatus(WorkOrderStatus.COMPLETED, "entregar o veículo");
		changeStatus(WorkOrderStatus.DELIVERED, deliveredBy, now);
		registerEvent(new VehicleDelivered(id, now));
	}

	public Optional<Estimate> currentEstimate() {
		return estimates.isEmpty() ? Optional.empty() : Optional.of(estimates.getLast());
	}

	/** Peças e insumos da OS somados por item de estoque. */
	public List<RequiredPart> requiredParts() {
		var totals = new LinkedHashMap<PartId, Quantity>();
		partItems.forEach(item -> totals.merge(item.getPartId(), item.getQuantity(),
				(current, added) -> new Quantity(current.value().add(added.value()))));
		return totals.entrySet().stream().map(entry -> new RequiredPart(entry.getKey(), entry.getValue())).toList();
	}

	private Estimate pendingEstimate() {
		if (status != WorkOrderStatus.AWAITING_APPROVAL) {
			throw new BusinessRuleException("Não há orçamento aguardando resposta nesta ordem de serviço.");
		}
		return currentEstimate().filter(Estimate::isPending)
				.orElseThrow(() -> new BusinessRuleException("O orçamento desta ordem de serviço já foi respondido."));
	}

	private void requireStatus(WorkOrderStatus expected, String action) {
		if (status != expected) {
			throw new BusinessRuleException("Não é possível %s: a ordem de serviço está \"%s\" e precisa estar \"%s\"."
					.formatted(action, status.label(), expected.label()));
		}
	}

	private void changeStatus(WorkOrderStatus newStatus, UserId changedBy, Instant now) {
		statusTransitions.add(StatusTransition.record(status, newStatus, changedBy, now));
		status = newStatus;
		updatedAt = now;
	}

	private void itemsChanged(Instant now) {
		totalAmount = calculateTotal();
		updatedAt = now;
	}

	private Money calculateTotal() {
		var labor = laborItems.stream().map(LaborItem::subtotal).reduce(Money.ZERO, Money::add);
		return partItems.stream().map(PartItem::subtotal).reduce(labor, Money::add);
	}

	public WorkOrderId getId() {
		return id;
	}

	public Long getNumber() {
		return number;
	}

	public TrackingCode getTrackingCode() {
		return trackingCode;
	}

	public CustomerId getCustomerId() {
		return customerId;
	}

	public VehicleId getVehicleId() {
		return vehicleId;
	}

	public WorkOrderStatus getStatus() {
		return status;
	}

	public String getProblemDescription() {
		return problemDescription;
	}

	public String getDiagnosisNotes() {
		return diagnosisNotes;
	}

	public Money getTotalAmount() {
		return totalAmount;
	}

	public Long getVersion() {
		return version;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public List<LaborItem> getLaborItems() {
		return Collections.unmodifiableList(laborItems);
	}

	public List<PartItem> getPartItems() {
		return Collections.unmodifiableList(partItems);
	}

	public List<Estimate> getEstimates() {
		return Collections.unmodifiableList(estimates);
	}

	public List<StatusTransition> getStatusTransitions() {
		return Collections.unmodifiableList(statusTransitions);
	}
}
