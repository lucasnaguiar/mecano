package br.dev.lucasaguiar.mecano.inventory.application;

import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.shared.domain.Quantity;
import br.dev.lucasaguiar.mecano.workorder.domain.event.EstimateApproved;
import br.dev.lucasaguiar.mecano.workorder.domain.event.ExecutionStarted;
import br.dev.lucasaguiar.mecano.workorder.domain.event.RequiredPart;
import br.dev.lucasaguiar.mecano.workorder.domain.event.WorkOrderCanceled;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Políticas "Reservar peças" (orçamento aprovado), "Baixar peças" (execução iniciada) e "Liberar reserva" (OS
 * cancelada com orçamento aprovado). Os listeners são síncronos e
 * rodam na transação da OS: se o estoque for insuficiente, a aprovação é desfeita e o cliente recebe o motivo.
 */
@Component
class WorkOrderStockPolicy {

	private final WorkOrderStockUseCase workOrderStockUseCase;

	WorkOrderStockPolicy(WorkOrderStockUseCase workOrderStockUseCase) {
		this.workOrderStockUseCase = workOrderStockUseCase;
	}

	@EventListener
	void on(EstimateApproved event) {
		workOrderStockUseCase.reserve(event.workOrderId().value(), quantities(event.parts()));
	}

	@EventListener
	void on(ExecutionStarted event) {
		workOrderStockUseCase.decrease(event.workOrderId().value(), quantities(event.parts()));
	}

	@EventListener
	void on(WorkOrderCanceled event) {
		if (!event.reservedParts().isEmpty()) {
			workOrderStockUseCase.release(event.workOrderId().value(), quantities(event.reservedParts()));
		}
	}

	private static Map<PartId, Quantity> quantities(List<RequiredPart> parts) {
		var quantities = new LinkedHashMap<PartId, Quantity>();
		parts.forEach(part -> quantities.put(part.partId(), part.quantity()));
		return quantities;
	}
}
