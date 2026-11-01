package br.dev.lucasaguiar.mecano.workorder.presentation.admin;

import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationId;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleId;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.shared.presentation.AuthenticatedUser;
import br.dev.lucasaguiar.mecano.shared.presentation.PageParams;
import br.dev.lucasaguiar.mecano.shared.presentation.PageResponse;
import br.dev.lucasaguiar.mecano.workorder.application.AddLaborUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.AddPartUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.CompleteWorkOrderUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.DeliverVehicleUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.GetWorkOrderUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.ListOngoingWorkOrdersUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.ListWorkOrdersUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.OpenWorkOrderUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.RemoveLaborItemUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.RemovePartItemUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.SendEstimateUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.StartDiagnosisUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.StartExecutionUseCase;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderStatus;
import br.dev.lucasaguiar.mecano.workorder.presentation.dto.AddLaborRequest;
import br.dev.lucasaguiar.mecano.workorder.presentation.dto.AddPartRequest;
import br.dev.lucasaguiar.mecano.workorder.presentation.dto.OpenWorkOrderRequest;
import br.dev.lucasaguiar.mecano.workorder.presentation.dto.SendEstimateRequest;
import br.dev.lucasaguiar.mecano.workorder.presentation.dto.WorkOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/work-orders")
@Tag(name = "Ordens de serviço", description = "Ciclo de atendimento: abertura, diagnóstico, orçamento, execução e entrega")
@SecurityRequirement(name = "bearer-jwt")
public class WorkOrderAdminController {

	private final OpenWorkOrderUseCase openWorkOrderUseCase;
	private final StartDiagnosisUseCase startDiagnosisUseCase;
	private final AddLaborUseCase addLaborUseCase;
	private final AddPartUseCase addPartUseCase;
	private final RemoveLaborItemUseCase removeLaborItemUseCase;
	private final RemovePartItemUseCase removePartItemUseCase;
	private final SendEstimateUseCase sendEstimateUseCase;
	private final StartExecutionUseCase startExecutionUseCase;
	private final CompleteWorkOrderUseCase completeWorkOrderUseCase;
	private final DeliverVehicleUseCase deliverVehicleUseCase;
	private final GetWorkOrderUseCase getWorkOrderUseCase;
	private final ListWorkOrdersUseCase listWorkOrdersUseCase;
	private final ListOngoingWorkOrdersUseCase listOngoingWorkOrdersUseCase;

	public WorkOrderAdminController(OpenWorkOrderUseCase openWorkOrderUseCase,
			StartDiagnosisUseCase startDiagnosisUseCase, AddLaborUseCase addLaborUseCase,
			AddPartUseCase addPartUseCase, RemoveLaborItemUseCase removeLaborItemUseCase,
			RemovePartItemUseCase removePartItemUseCase, SendEstimateUseCase sendEstimateUseCase,
			StartExecutionUseCase startExecutionUseCase, CompleteWorkOrderUseCase completeWorkOrderUseCase,
			DeliverVehicleUseCase deliverVehicleUseCase, GetWorkOrderUseCase getWorkOrderUseCase,
			ListWorkOrdersUseCase listWorkOrdersUseCase, ListOngoingWorkOrdersUseCase listOngoingWorkOrdersUseCase) {
		this.openWorkOrderUseCase = openWorkOrderUseCase;
		this.startDiagnosisUseCase = startDiagnosisUseCase;
		this.addLaborUseCase = addLaborUseCase;
		this.addPartUseCase = addPartUseCase;
		this.removeLaborItemUseCase = removeLaborItemUseCase;
		this.removePartItemUseCase = removePartItemUseCase;
		this.sendEstimateUseCase = sendEstimateUseCase;
		this.startExecutionUseCase = startExecutionUseCase;
		this.completeWorkOrderUseCase = completeWorkOrderUseCase;
		this.deliverVehicleUseCase = deliverVehicleUseCase;
		this.getWorkOrderUseCase = getWorkOrderUseCase;
		this.listWorkOrdersUseCase = listWorkOrdersUseCase;
		this.listOngoingWorkOrdersUseCase = listOngoingWorkOrdersUseCase;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Abre uma ordem de serviço para o veículo do cliente")
	public WorkOrderResponse open(@RequestBody @Valid OpenWorkOrderRequest request, @AuthenticationPrincipal Jwt jwt) {
		return WorkOrderResponse.from(openWorkOrderUseCase.execute(new CustomerId(request.customerId()),
				new VehicleId(request.vehicleId()), request.problemDescription(), AuthenticatedUser.idOf(jwt)));
	}

	@GetMapping
	@Operation(summary = "Lista as ordens de serviço, opcionalmente por status")
	public PageResponse<WorkOrderResponse> list(@RequestParam(required = false) WorkOrderStatus status,
			PageParams pageParams) {
		return PageResponse.from(listWorkOrdersUseCase.execute(status, pageParams.toQuery()), WorkOrderResponse::from);
	}

	@GetMapping("/ongoing")
	@Operation(summary = "Fila de OS em andamento",
			description = "Em execução, aguardando aprovação, em diagnóstico e recebidas, nessa ordem; dentro de cada "
					+ "status, das mais antigas para as mais recentes. Finalizadas e entregues ficam de fora.")
	public PageResponse<WorkOrderResponse> ongoing(PageParams pageParams) {
		return PageResponse.from(listOngoingWorkOrdersUseCase.execute(pageParams.toQuery()), WorkOrderResponse::from);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta uma ordem de serviço")
	public WorkOrderResponse get(@PathVariable UUID id) {
		return WorkOrderResponse.from(getWorkOrderUseCase.execute(new WorkOrderId(id)));
	}

	@PostMapping("/{id}/diagnosis")
	@Operation(summary = "Inicia o diagnóstico")
	public WorkOrderResponse startDiagnosis(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
		return WorkOrderResponse.from(startDiagnosisUseCase.execute(new WorkOrderId(id), AuthenticatedUser.idOf(jwt)));
	}

	@PostMapping("/{id}/labor-items")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Inclui um serviço do catálogo (somente em diagnóstico)")
	public WorkOrderResponse addLabor(@PathVariable UUID id, @RequestBody @Valid AddLaborRequest request) {
		return WorkOrderResponse.from(addLaborUseCase.execute(new WorkOrderId(id),
				new LaborOperationId(request.laborOperationId()), request.quantityOrDefault()));
	}

	@DeleteMapping("/{id}/labor-items/{itemId}")
	@Operation(summary = "Remove um serviço (somente em diagnóstico)")
	public WorkOrderResponse removeLabor(@PathVariable UUID id, @PathVariable UUID itemId) {
		return WorkOrderResponse.from(removeLaborItemUseCase.execute(new WorkOrderId(id), itemId));
	}

	@PostMapping("/{id}/part-items")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Inclui uma peça ou insumo (somente em diagnóstico)")
	public WorkOrderResponse addPart(@PathVariable UUID id, @RequestBody @Valid AddPartRequest request) {
		return WorkOrderResponse.from(addPartUseCase.execute(new WorkOrderId(id), new PartId(request.partId()),
				request.quantity()));
	}

	@DeleteMapping("/{id}/part-items/{itemId}")
	@Operation(summary = "Remove uma peça ou insumo (somente em diagnóstico)")
	public WorkOrderResponse removePart(@PathVariable UUID id, @PathVariable UUID itemId) {
		return WorkOrderResponse.from(removePartItemUseCase.execute(new WorkOrderId(id), itemId));
	}

	@PostMapping("/{id}/estimate")
	@Operation(summary = "Envia o orçamento para aprovação do cliente")
	public WorkOrderResponse sendEstimate(@PathVariable UUID id, @RequestBody(required = false) SendEstimateRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		var notes = request == null ? null : request.diagnosisNotes();
		return WorkOrderResponse.from(sendEstimateUseCase.execute(new WorkOrderId(id), notes,
				AuthenticatedUser.idOf(jwt)));
	}

	@PostMapping("/{id}/execution")
	@Operation(summary = "Inicia a execução (orçamento aprovado) e baixa as peças reservadas")
	public WorkOrderResponse startExecution(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
		return WorkOrderResponse.from(startExecutionUseCase.execute(new WorkOrderId(id), AuthenticatedUser.idOf(jwt)));
	}

	@PostMapping("/{id}/completion")
	@Operation(summary = "Finaliza o serviço")
	public WorkOrderResponse complete(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
		return WorkOrderResponse.from(completeWorkOrderUseCase.execute(new WorkOrderId(id),
				AuthenticatedUser.idOf(jwt)));
	}

	@PostMapping("/{id}/delivery")
	@Operation(summary = "Registra a entrega do veículo ao cliente")
	public WorkOrderResponse deliver(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
		return WorkOrderResponse.from(deliverVehicleUseCase.execute(new WorkOrderId(id), AuthenticatedUser.idOf(jwt)));
	}
}
