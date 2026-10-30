package br.dev.lucasaguiar.mecano.inventory.presentation;

import br.dev.lucasaguiar.mecano.inventory.application.ChangePartPriceUseCase;
import br.dev.lucasaguiar.mecano.inventory.application.DeactivatePartUseCase;
import br.dev.lucasaguiar.mecano.inventory.application.GetPartUseCase;
import br.dev.lucasaguiar.mecano.inventory.application.ListPartsUseCase;
import br.dev.lucasaguiar.mecano.inventory.application.ListStockMovementsUseCase;
import br.dev.lucasaguiar.mecano.inventory.application.RegisterPartUseCase;
import br.dev.lucasaguiar.mecano.inventory.application.RegisterStockEntryUseCase;
import br.dev.lucasaguiar.mecano.inventory.application.UpdatePartUseCase;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.inventory.presentation.dto.ChangePriceRequest;
import br.dev.lucasaguiar.mecano.inventory.presentation.dto.CreatePartRequest;
import br.dev.lucasaguiar.mecano.inventory.presentation.dto.PartResponse;
import br.dev.lucasaguiar.mecano.inventory.presentation.dto.StockEntryRequest;
import br.dev.lucasaguiar.mecano.inventory.presentation.dto.StockMovementResponse;
import br.dev.lucasaguiar.mecano.inventory.presentation.dto.UpdatePartRequest;
import br.dev.lucasaguiar.mecano.shared.presentation.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
@RequestMapping("/api/admin/parts")
@Tag(name = "Estoque", description = "Peças e insumos (alteração somente ADMIN e STOCK_MANAGER)")
@SecurityRequirement(name = "bearer-jwt")
public class PartController {

	private final RegisterPartUseCase registerPartUseCase;
	private final UpdatePartUseCase updatePartUseCase;
	private final ChangePartPriceUseCase changePartPriceUseCase;
	private final RegisterStockEntryUseCase registerStockEntryUseCase;
	private final DeactivatePartUseCase deactivatePartUseCase;
	private final GetPartUseCase getPartUseCase;
	private final ListPartsUseCase listPartsUseCase;
	private final ListStockMovementsUseCase listStockMovementsUseCase;

	public PartController(RegisterPartUseCase registerPartUseCase, UpdatePartUseCase updatePartUseCase,
			ChangePartPriceUseCase changePartPriceUseCase, RegisterStockEntryUseCase registerStockEntryUseCase,
			DeactivatePartUseCase deactivatePartUseCase, GetPartUseCase getPartUseCase,
			ListPartsUseCase listPartsUseCase, ListStockMovementsUseCase listStockMovementsUseCase) {
		this.registerPartUseCase = registerPartUseCase;
		this.updatePartUseCase = updatePartUseCase;
		this.changePartPriceUseCase = changePartPriceUseCase;
		this.registerStockEntryUseCase = registerStockEntryUseCase;
		this.deactivatePartUseCase = deactivatePartUseCase;
		this.getPartUseCase = getPartUseCase;
		this.listPartsUseCase = listPartsUseCase;
		this.listStockMovementsUseCase = listStockMovementsUseCase;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cadastra uma peça ou insumo")
	public PartResponse register(@RequestBody @Valid CreatePartRequest request) {
		return PartResponse.from(registerPartUseCase.execute(request.toCommand()));
	}

	@GetMapping
	@Operation(summary = "Lista peças e insumos")
	public List<PartResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive,
			@RequestParam(defaultValue = "false") boolean atOrBelowMinimum) {
		return listPartsUseCase.execute(includeInactive, atOrBelowMinimum).stream().map(PartResponse::from).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta uma peça ou insumo")
	public PartResponse get(@PathVariable UUID id) {
		return PartResponse.from(getPartUseCase.execute(new PartId(id)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Altera nome e estoque mínimo")
	public PartResponse update(@PathVariable UUID id, @RequestBody @Valid UpdatePartRequest request) {
		return PartResponse.from(updatePartUseCase.execute(new PartId(id), request.name(), request.minimumStock()));
	}

	@PutMapping("/{id}/price")
	@Operation(summary = "Altera o preço de venda (não afeta OS já orçadas)")
	public PartResponse changePrice(@PathVariable UUID id, @RequestBody @Valid ChangePriceRequest request) {
		return PartResponse.from(changePartPriceUseCase.execute(new PartId(id), request.unitPrice()));
	}

	@PostMapping("/{id}/entries")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Registra a entrada de unidades no estoque")
	public PartResponse registerEntry(@PathVariable UUID id, @RequestBody @Valid StockEntryRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return PartResponse.from(registerStockEntryUseCase.execute(new PartId(id), request.quantity(),
				AuthenticatedUser.idOf(jwt)));
	}

	@GetMapping("/{id}/movements")
	@Operation(summary = "Extrato de movimentações do item")
	public List<StockMovementResponse> movements(@PathVariable UUID id) {
		return listStockMovementsUseCase.execute(new PartId(id)).stream().map(StockMovementResponse::from).toList();
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Desativa uma peça ou insumo")
	public void deactivate(@PathVariable UUID id) {
		deactivatePartUseCase.execute(new PartId(id));
	}
}
