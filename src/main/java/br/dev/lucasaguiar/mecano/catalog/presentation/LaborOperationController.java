package br.dev.lucasaguiar.mecano.catalog.presentation;

import br.dev.lucasaguiar.mecano.catalog.application.DeactivateLaborOperationUseCase;
import br.dev.lucasaguiar.mecano.catalog.application.GetLaborOperationUseCase;
import br.dev.lucasaguiar.mecano.catalog.application.ListLaborOperationsUseCase;
import br.dev.lucasaguiar.mecano.catalog.application.RegisterLaborOperationUseCase;
import br.dev.lucasaguiar.mecano.catalog.application.UpdateLaborOperationUseCase;
import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationId;
import br.dev.lucasaguiar.mecano.catalog.presentation.dto.LaborOperationRequest;
import br.dev.lucasaguiar.mecano.catalog.presentation.dto.LaborOperationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/labor-operations")
@Tag(name = "Catálogo de serviços", description = "Serviços oferecidos pela oficina (alteração somente ADMIN)")
@SecurityRequirement(name = "bearer-jwt")
public class LaborOperationController {

	private final RegisterLaborOperationUseCase registerUseCase;
	private final UpdateLaborOperationUseCase updateUseCase;
	private final DeactivateLaborOperationUseCase deactivateUseCase;
	private final GetLaborOperationUseCase getUseCase;
	private final ListLaborOperationsUseCase listUseCase;

	public LaborOperationController(RegisterLaborOperationUseCase registerUseCase,
			UpdateLaborOperationUseCase updateUseCase, DeactivateLaborOperationUseCase deactivateUseCase,
			GetLaborOperationUseCase getUseCase, ListLaborOperationsUseCase listUseCase) {
		this.registerUseCase = registerUseCase;
		this.updateUseCase = updateUseCase;
		this.deactivateUseCase = deactivateUseCase;
		this.getUseCase = getUseCase;
		this.listUseCase = listUseCase;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cadastra um serviço no catálogo")
	public LaborOperationResponse register(@RequestBody @Valid LaborOperationRequest request) {
		return LaborOperationResponse.from(registerUseCase.execute(request.toData()));
	}

	@GetMapping
	@Operation(summary = "Lista os serviços do catálogo")
	public List<LaborOperationResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
		return listUseCase.execute(includeInactive).stream().map(LaborOperationResponse::from).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta um serviço do catálogo")
	public LaborOperationResponse get(@PathVariable UUID id) {
		return LaborOperationResponse.from(getUseCase.execute(new LaborOperationId(id)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Altera um serviço do catálogo (não afeta OS já orçadas)")
	public LaborOperationResponse update(@PathVariable UUID id, @RequestBody @Valid LaborOperationRequest request) {
		return LaborOperationResponse.from(updateUseCase.execute(new LaborOperationId(id), request.toData()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Desativa um serviço do catálogo")
	public void deactivate(@PathVariable UUID id) {
		deactivateUseCase.execute(new LaborOperationId(id));
	}
}
