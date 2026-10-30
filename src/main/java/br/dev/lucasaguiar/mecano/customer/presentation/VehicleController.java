package br.dev.lucasaguiar.mecano.customer.presentation;

import br.dev.lucasaguiar.mecano.customer.application.DeleteVehicleUseCase;
import br.dev.lucasaguiar.mecano.customer.application.FindVehicleByLicensePlateUseCase;
import br.dev.lucasaguiar.mecano.customer.application.GetVehicleUseCase;
import br.dev.lucasaguiar.mecano.customer.application.RegisterVehicleUseCase;
import br.dev.lucasaguiar.mecano.customer.application.UpdateVehicleUseCase;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleId;
import br.dev.lucasaguiar.mecano.customer.presentation.dto.CreateVehicleRequest;
import br.dev.lucasaguiar.mecano.customer.presentation.dto.UpdateVehicleRequest;
import br.dev.lucasaguiar.mecano.customer.presentation.dto.VehicleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/admin/vehicles")
@Tag(name = "Veículos", description = "Cadastro de veículos dos clientes")
@SecurityRequirement(name = "bearer-jwt")
public class VehicleController {

	private final RegisterVehicleUseCase registerVehicleUseCase;
	private final UpdateVehicleUseCase updateVehicleUseCase;
	private final GetVehicleUseCase getVehicleUseCase;
	private final FindVehicleByLicensePlateUseCase findVehicleByLicensePlateUseCase;
	private final DeleteVehicleUseCase deleteVehicleUseCase;

	public VehicleController(RegisterVehicleUseCase registerVehicleUseCase, UpdateVehicleUseCase updateVehicleUseCase,
			GetVehicleUseCase getVehicleUseCase, FindVehicleByLicensePlateUseCase findVehicleByLicensePlateUseCase,
			DeleteVehicleUseCase deleteVehicleUseCase) {
		this.registerVehicleUseCase = registerVehicleUseCase;
		this.updateVehicleUseCase = updateVehicleUseCase;
		this.getVehicleUseCase = getVehicleUseCase;
		this.findVehicleByLicensePlateUseCase = findVehicleByLicensePlateUseCase;
		this.deleteVehicleUseCase = deleteVehicleUseCase;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cadastra um veículo para um cliente")
	public VehicleResponse register(@RequestBody @Valid CreateVehicleRequest request) {
		return VehicleResponse.from(registerVehicleUseCase.execute(new CustomerId(request.customerId()),
				request.licensePlate(), request.toData()));
	}

	@GetMapping(params = "licensePlate")
	@Operation(summary = "Busca um veículo pela placa")
	public VehicleResponse findByLicensePlate(@RequestParam String licensePlate) {
		return VehicleResponse.from(findVehicleByLicensePlateUseCase.execute(licensePlate));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta um veículo")
	public VehicleResponse get(@PathVariable UUID id) {
		return VehicleResponse.from(getVehicleUseCase.execute(new VehicleId(id)));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Altera marca, modelo e ano do veículo")
	public VehicleResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateVehicleRequest request) {
		return VehicleResponse.from(updateVehicleUseCase.execute(new VehicleId(id), request.toData()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remove um veículo sem ordens de serviço")
	public void delete(@PathVariable UUID id) {
		deleteVehicleUseCase.execute(new VehicleId(id));
	}
}
