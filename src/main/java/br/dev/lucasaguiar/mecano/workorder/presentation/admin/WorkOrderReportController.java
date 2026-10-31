package br.dev.lucasaguiar.mecano.workorder.presentation.admin;

import br.dev.lucasaguiar.mecano.workorder.application.GetAverageExecutionTimeUseCase;
import br.dev.lucasaguiar.mecano.workorder.presentation.dto.AverageExecutionTimeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
@Tag(name = "Indicadores", description = "Indicadores da oficina (somente ADMIN)")
@SecurityRequirement(name = "bearer-jwt")
public class WorkOrderReportController {

	private final GetAverageExecutionTimeUseCase getAverageExecutionTimeUseCase;

	public WorkOrderReportController(GetAverageExecutionTimeUseCase getAverageExecutionTimeUseCase) {
		this.getAverageExecutionTimeUseCase = getAverageExecutionTimeUseCase;
	}

	@GetMapping("/average-execution-time")
	@Operation(summary = "Tempo médio de execução dos serviços")
	public AverageExecutionTimeResponse averageExecutionTime() {
		return getAverageExecutionTimeUseCase.execute()
				.map(AverageExecutionTimeResponse::from)
				.orElseGet(AverageExecutionTimeResponse::empty);
	}
}
