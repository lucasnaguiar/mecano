package br.dev.lucasaguiar.mecano.shared.presentation;

import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Extrai o usuário da equipe a partir do token JWT (subject = ID do usuário).
 */
public final class AuthenticatedUser {

	private AuthenticatedUser() {
	}

	public static UserId idOf(Jwt jwt) {
		return new UserId(UUID.fromString(jwt.getSubject()));
	}
}
