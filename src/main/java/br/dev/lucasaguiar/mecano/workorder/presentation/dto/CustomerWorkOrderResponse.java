package br.dev.lucasaguiar.mecano.workorder.presentation.dto;

import br.dev.lucasaguiar.mecano.shared.domain.UnitOfMeasure;
import br.dev.lucasaguiar.mecano.workorder.domain.EstimateStatus;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrder;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

@Schema(description = "Andamento da ordem de serviço (visão do cliente, sem dados internos)")
public record CustomerWorkOrderResponse(Long number, WorkOrderStatus status, String statusLabel,
		String problemDescription, String diagnosisNotes, List<Item> items, BigDecimal totalAmount,
		Estimate estimate, List<Step> history) {

	public static CustomerWorkOrderResponse from(WorkOrder workOrder) {
		var labor = workOrder.getLaborItems().stream().map(item -> new Item(item.getDescription(),
				BigDecimal.valueOf(item.getQuantity()), null, item.getUnitPrice().amount(), item.subtotal().amount()));
		var parts = workOrder.getPartItems().stream().map(item -> new Item(item.getDescription(),
				item.getQuantity().value(), item.getUnitOfMeasure(), item.getUnitPrice().amount(),
				item.subtotal().amount()));
		return new CustomerWorkOrderResponse(workOrder.getNumber(), workOrder.getStatus(),
				workOrder.getStatus().label(), workOrder.getProblemDescription(), workOrder.getDiagnosisNotes(),
				Stream.concat(labor, parts).toList(), workOrder.getTotalAmount().amount(),
				workOrder.currentEstimate().map(estimate -> new Estimate(estimate.getStatus(),
						estimate.getTotalAmount().amount(), estimate.getSentAt(), estimate.getRespondedAt()))
						.orElse(null),
				workOrder.getStatusTransitions().stream().map(transition -> new Step(transition.getToStatus(),
						transition.getToStatus().label(), transition.getOccurredAt())).toList());
	}

	public record Item(String description, BigDecimal quantity, UnitOfMeasure unitOfMeasure, BigDecimal unitPrice,
			BigDecimal subtotal) {
	}

	public record Estimate(EstimateStatus status, BigDecimal totalAmount, Instant sentAt, Instant respondedAt) {
	}

	public record Step(WorkOrderStatus status, String label, Instant occurredAt) {
	}
}
