package br.dev.lucasaguiar.mecano.auth.presentation;

import br.dev.lucasaguiar.mecano.auth.application.RegisterUserUseCase;
import br.dev.lucasaguiar.mecano.auth.presentation.dto.RegisterUserRequest;
import br.dev.lucasaguiar.mecano.auth.presentation.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Usuários", description = "Gestão dos usuários da equipe (somente ADMIN)")
@SecurityRequirement(name = "bearer-jwt")
public class UserAdminController {

	private final RegisterUserUseCase registerUserUseCase;

	public UserAdminController(RegisterUserUseCase registerUserUseCase) {
		this.registerUserUseCase = registerUserUseCase;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cadastra um usuário da equipe")
	@ApiResponse(responseCode = "201", description = "Usuário cadastrado")
	@ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
	public UserResponse register(@RequestBody @Valid RegisterUserRequest request) {
		return UserResponse.from(registerUserUseCase.execute(request.toCommand()));
	}
}
