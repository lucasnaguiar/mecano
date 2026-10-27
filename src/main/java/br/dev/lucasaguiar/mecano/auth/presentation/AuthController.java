package br.dev.lucasaguiar.mecano.auth.presentation;

import br.dev.lucasaguiar.mecano.auth.application.LoginUseCase;
import br.dev.lucasaguiar.mecano.auth.presentation.dto.LoginRequest;
import br.dev.lucasaguiar.mecano.auth.presentation.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Login da equipe da oficina")
public class AuthController {

	private final LoginUseCase loginUseCase;

	public AuthController(LoginUseCase loginUseCase) {
		this.loginUseCase = loginUseCase;
	}

	@PostMapping("/login")
	@Operation(summary = "Autentica um usuário e retorna o token JWT")
	@ApiResponse(responseCode = "200", description = "Login realizado")
	@ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos")
	public TokenResponse login(@RequestBody @Valid LoginRequest request) {
		return TokenResponse.from(loginUseCase.execute(request.email(), request.password()));
	}
}
