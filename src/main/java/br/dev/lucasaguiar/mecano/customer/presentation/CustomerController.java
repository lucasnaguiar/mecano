package br.dev.lucasaguiar.mecano.customer.presentation;

import br.dev.lucasaguiar.mecano.customer.application.DeleteCustomerUseCase;
import br.dev.lucasaguiar.mecano.customer.application.GetCustomerUseCase;
import br.dev.lucasaguiar.mecano.customer.application.ListCustomerVehiclesUseCase;
import br.dev.lucasaguiar.mecano.customer.application.ListCustomersUseCase;
import br.dev.lucasaguiar.mecano.customer.application.RegisterCustomerUseCase;
import br.dev.lucasaguiar.mecano.customer.application.UpdateCustomerUseCase;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.presentation.dto.CreateCustomerRequest;
import br.dev.lucasaguiar.mecano.customer.presentation.dto.CustomerResponse;
import br.dev.lucasaguiar.mecano.customer.presentation.dto.UpdateCustomerRequest;
import br.dev.lucasaguiar.mecano.customer.presentation.dto.VehicleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/admin/customers")
@Tag(name = "Clientes", description = "Cadastro de clientes da oficina")
@SecurityRequirement(name = "bearer-jwt")
public class CustomerController {

	private final RegisterCustomerUseCase registerCustomerUseCase;
	private final UpdateCustomerUseCase updateCustomerUseCase;
	private final GetCustomerUseCase getCustomerUseCase;
	private final ListCustomersUseCase listCustomersUseCase;
	private final DeleteCustomerUseCase deleteCustomerUseCase;
	private final ListCustomerVehiclesUseCase listCustomerVehiclesUseCase;

	public CustomerController(RegisterCustomerUseCase registerCustomerUseCase,
			UpdateCustomerUseCase updateCustomerUseCase, GetCustomerUseCase getCustomerUseCase,
			ListCustomersUseCase listCustomersUseCase, DeleteCustomerUseCase deleteCustomerUseCase,
			ListCustomerVehiclesUseCase listCustomerVehiclesUseCase) {
		this.registerCustomerUseCase = registerCustomerUseCase;
		this.updateCustomerUseCase = updateCustomerUseCase;
		this.getCustomerUseCase = getCustomerUseCase;
		this.listCustomersUseCase = listCustomersUseCase;
		this.deleteCustomerUseCase = deleteCustomerUseCase;
		this.listCustomerVehiclesUseCase = listCustomerVehiclesUseCase;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cadastra um cliente")
	public CustomerResponse register(@RequestBody @Valid CreateCustomerRequest request) {
		return CustomerResponse.from(registerCustomerUseCase.execute(request.taxId(), request.toData()));
	}

	@GetMapping
	@Operation(summary = "Lista os clientes ou busca pelo CPF/CNPJ")
	public List<CustomerResponse> list(
			@Parameter(description = "CPF ou CNPJ, com ou sem pontuação") @RequestParam(required = false) String taxId) {
		return listCustomersUseCase.execute(taxId).stream().map(CustomerResponse::from).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta um cliente")
	public CustomerResponse get(@PathVariable UUID id) {
		return CustomerResponse.from(getCustomerUseCase.execute(new CustomerId(id)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Altera nome e contatos do cliente")
	public CustomerResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateCustomerRequest request) {
		return CustomerResponse.from(updateCustomerUseCase.execute(new CustomerId(id), request.toData()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remove um cliente sem veículos cadastrados")
	public void delete(@PathVariable UUID id) {
		deleteCustomerUseCase.execute(new CustomerId(id));
	}

	@GetMapping("/{id}/vehicles")
	@Operation(summary = "Lista os veículos do cliente")
	public List<VehicleResponse> vehicles(@PathVariable UUID id) {
		return listCustomerVehiclesUseCase.execute(new CustomerId(id)).stream().map(VehicleResponse::from).toList();
	}
}
