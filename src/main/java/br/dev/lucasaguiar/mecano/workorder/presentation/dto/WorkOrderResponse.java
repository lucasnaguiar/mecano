package br.dev.lucasaguiar.mecano.workorder.presentation.dto;

import br.dev.lucasaguiar.mecano.shared.domain.UnitOfMeasure;
import br.dev.lucasaguiar.mecano.workorder.domain.EstimateStatus;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Ordem de serviço (visão da equipe)")
public record WorkOrderResponse(UUID id, Long number, String trackingCode, UUID customerId, UUID vehicleId,
		WorkOrderStatus status, String statusLabel, String problemDescription, String diagnosisNotes,
		String cancellationReason, BigDecimal totalAmount, List<LaborItemResponse> laborItems, List<PartItemResponse> partItems,
		List<EstimateResponse> estimates, List<StatusTransitionResponse> statusHistory, Instant createdAt,
		Instant updatedAt) {

	public static WorkOrderResponse from(WorkOrder workOrder) {
		return new WorkOrderResponse(workOrder.getId().value(), workOrder.getNumber(),
				workOrder.getTrackingCode().value(), workOrder.getCustomerId().value(),
				workOrder.getVehicleId().value(), workOrder.getStatus(), workOrder.getStatus().label(),
				workOrder.getProblemDescription(), workOrder.getDiagnosisNotes(), workOrder.getCancellationReason(),
				workOrder.getTotalAmount().amount(),
				workOrder.getLaborItems().stream().map(item -> new LaborItemResponse(item.getId(),
						item.getLaborOperationId().value(), item.getDescription(), item.getQuantity(),
						item.getUnitPrice().amount(), item.subtotal().amount())).toList(),
				workOrder.getPartItems().stream().map(item -> new PartItemResponse(item.getId(),
						item.getPartId().value(), item.getDescription(), item.getQuantity().value(),
						item.getUnitOfMeasure(), item.getUnitPrice().amount(), item.subtotal().amount())).toList(),
				workOrder.getEstimates().stream().map(estimate -> new EstimateResponse(estimate.getId(),
						estimate.getStatus(), estimate.getTotalAmount().amount(), estimate.getSentAt(),
						estimate.getRespondedAt())).toList(),
				workOrder.getStatusTransitions().stream().map(transition -> new StatusTransitionResponse(
						transition.getFromStatus(), transition.getToStatus(), transition.getToStatus().label(),
						transition.getChangedBy() == null ? null : transition.getChangedBy().value(),
						transition.getOccurredAt())).toList(),
				workOrder.getCreatedAt(), workOrder.getUpdatedAt());
	}

	public record LaborItemResponse(UUID id, UUID laborOperationId, String description, int quantity,
			BigDecimal unitPrice, BigDecimal subtotal) {
	}

	public record PartItemResponse(UUID id, UUID partId, String description, BigDecimal quantity,
			UnitOfMeasure unitOfMeasure, BigDecimal unitPrice, BigDecimal subtotal) {
	}

	public record EstimateResponse(UUID id, EstimateStatus status, BigDecimal totalAmount, Instant sentAt,
			Instant respondedAt) {
	}

	public record StatusTransitionResponse(WorkOrderStatus fromStatus, WorkOrderStatus toStatus, String toStatusLabel,
			UUID changedBy, Instant occurredAt) {
	}
}
