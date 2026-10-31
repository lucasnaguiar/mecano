package br.dev.lucasaguiar.mecano.workorder.presentation.customer;

import br.dev.lucasaguiar.mecano.workorder.application.ApproveEstimateUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.RejectEstimateUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.TrackWorkOrderUseCase;
import br.dev.lucasaguiar.mecano.workorder.presentation.dto.CustomerWorkOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/work-orders/{trackingCode}")
@Tag(name = "Acompanhamento pelo cliente",
		description = "Consulta e resposta ao orçamento pelo código de acompanhamento, sem login")
public class WorkOrderCustomerController {

	private final TrackWorkOrderUseCase trackWorkOrderUseCase;
	private final ApproveEstimateUseCase approveEstimateUseCase;
	private final RejectEstimateUseCase rejectEstimateUseCase;

	public WorkOrderCustomerController(TrackWorkOrderUseCase trackWorkOrderUseCase,
			ApproveEstimateUseCase approveEstimateUseCase, RejectEstimateUseCase rejectEstimateUseCase) {
		this.trackWorkOrderUseCase = trackWorkOrderUseCase;
		this.approveEstimateUseCase = approveEstimateUseCase;
		this.rejectEstimateUseCase = rejectEstimateUseCase;
	}

	@GetMapping
	@Operation(summary = "Consulta o andamento da ordem de serviço")
	public CustomerWorkOrderResponse track(
			@Parameter(description = "Código de acompanhamento recebido do atendente") @PathVariable String trackingCode) {
		return CustomerWorkOrderResponse.from(trackWorkOrderUseCase.execute(trackingCode));
	}

	@PostMapping("/approval")
	@Operation(summary = "Aprova o orçamento (as peças são reservadas no estoque)")
	public CustomerWorkOrderResponse approve(@PathVariable String trackingCode) {
		return CustomerWorkOrderResponse.from(approveEstimateUseCase.execute(trackingCode));
	}

	@PostMapping("/rejection")
	@Operation(summary = "Recusa o orçamento (a OS volta ao diagnóstico)")
	public CustomerWorkOrderResponse reject(@PathVariable String trackingCode) {
		return CustomerWorkOrderResponse.from(rejectEstimateUseCase.execute(trackingCode));
	}
}
