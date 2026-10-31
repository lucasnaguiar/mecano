package br.dev.lucasaguiar.mecano.auth.presentation;

import br.dev.lucasaguiar.mecano.auth.application.ActivateUserUseCase;
import br.dev.lucasaguiar.mecano.auth.application.ChangeUserRoleUseCase;
import br.dev.lucasaguiar.mecano.auth.application.DeactivateUserUseCase;
import br.dev.lucasaguiar.mecano.auth.application.ListUsersUseCase;
import br.dev.lucasaguiar.mecano.auth.application.RegisterUserUseCase;
import br.dev.lucasaguiar.mecano.auth.presentation.dto.ChangeRoleRequest;
import br.dev.lucasaguiar.mecano.auth.presentation.dto.RegisterUserRequest;
import br.dev.lucasaguiar.mecano.auth.presentation.dto.UserResponse;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import br.dev.lucasaguiar.mecano.shared.presentation.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
	private final ListUsersUseCase listUsersUseCase;
	private final ChangeUserRoleUseCase changeUserRoleUseCase;
	private final DeactivateUserUseCase deactivateUserUseCase;
	private final ActivateUserUseCase activateUserUseCase;

	public UserAdminController(RegisterUserUseCase registerUserUseCase, ListUsersUseCase listUsersUseCase,
			ChangeUserRoleUseCase changeUserRoleUseCase, DeactivateUserUseCase deactivateUserUseCase,
			ActivateUserUseCase activateUserUseCase) {
		this.registerUserUseCase = registerUserUseCase;
		this.listUsersUseCase = listUsersUseCase;
		this.changeUserRoleUseCase = changeUserRoleUseCase;
		this.deactivateUserUseCase = deactivateUserUseCase;
		this.activateUserUseCase = activateUserUseCase;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cadastra um usuário da equipe")
	@ApiResponse(responseCode = "201", description = "Usuário cadastrado")
	@ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
	public UserResponse register(@RequestBody @Valid RegisterUserRequest request) {
		return UserResponse.from(registerUserUseCase.execute(request.toCommand()));
	}

	@GetMapping
	@Operation(summary = "Lista os usuários da equipe, ativos e inativos")
	public List<UserResponse> list() {
		return listUsersUseCase.execute().stream().map(UserResponse::from).toList();
	}

	@PutMapping("/{id}/role")
	@Operation(summary = "Altera o perfil de outro usuário (vale a partir do próximo login)")
	@ApiResponse(responseCode = "422", description = "Alteração do próprio perfil ou remoção do último ADMIN ativo")
	public UserResponse changeRole(@PathVariable UUID id, @RequestBody @Valid ChangeRoleRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return UserResponse.from(changeUserRoleUseCase.execute(new UserId(id), request.role(),
				AuthenticatedUser.idOf(jwt)));
	}

	@PostMapping("/{id}/deactivation")
	@Operation(summary = "Desativa outro usuário (sem login, histórico mantido)")
	@ApiResponse(responseCode = "422", description = "Desativação do próprio usuário ou do último ADMIN ativo")
	public UserResponse deactivate(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
		return UserResponse.from(deactivateUserUseCase.execute(new UserId(id), AuthenticatedUser.idOf(jwt)));
	}

	@PostMapping("/{id}/activation")
	@Operation(summary = "Reativa um usuário desativado")
	public UserResponse activate(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
		return UserResponse.from(activateUserUseCase.execute(new UserId(id), AuthenticatedUser.idOf(jwt)));
	}
}
