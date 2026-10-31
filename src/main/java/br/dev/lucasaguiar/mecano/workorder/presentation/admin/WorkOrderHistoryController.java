package br.dev.lucasaguiar.mecano.workorder.presentation.admin;

import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleId;
import br.dev.lucasaguiar.mecano.shared.presentation.PageParams;
import br.dev.lucasaguiar.mecano.shared.presentation.PageResponse;
import br.dev.lucasaguiar.mecano.workorder.application.ListCustomerWorkOrdersUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.ListVehicleWorkOrdersUseCase;
import br.dev.lucasaguiar.mecano.workorder.presentation.dto.WorkOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Ordens de serviço", description = "Ciclo de atendimento: abertura, diagnóstico, orçamento, execução e entrega")
@SecurityRequirement(name = "bearer-jwt")
public class WorkOrderHistoryController {

	private final ListCustomerWorkOrdersUseCase listCustomerWorkOrdersUseCase;
	private final ListVehicleWorkOrdersUseCase listVehicleWorkOrdersUseCase;

	public WorkOrderHistoryController(ListCustomerWorkOrdersUseCase listCustomerWorkOrdersUseCase,
			ListVehicleWorkOrdersUseCase listVehicleWorkOrdersUseCase) {
		this.listCustomerWorkOrdersUseCase = listCustomerWorkOrdersUseCase;
		this.listVehicleWorkOrdersUseCase = listVehicleWorkOrdersUseCase;
	}

	@GetMapping("/customers/{id}/work-orders")
	@Operation(summary = "Lista as ordens de serviço do cliente, das mais recentes para as mais antigas")
	public PageResponse<WorkOrderResponse> byCustomer(@PathVariable UUID id, PageParams pageParams) {
		return PageResponse.from(listCustomerWorkOrdersUseCase.execute(new CustomerId(id), pageParams.toQuery()),
				WorkOrderResponse::from);
	}

	@GetMapping("/vehicles/{id}/work-orders")
	@Operation(summary = "Lista as ordens de serviço do veículo, das mais recentes para as mais antigas")
	public PageResponse<WorkOrderResponse> byVehicle(@PathVariable UUID id, PageParams pageParams) {
		return PageResponse.from(listVehicleWorkOrdersUseCase.execute(new VehicleId(id), pageParams.toQuery()),
				WorkOrderResponse::from);
	}
}
