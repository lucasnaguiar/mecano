package br.dev.lucasaguiar.mecano.auth.presentation.dto;

import br.dev.lucasaguiar.mecano.auth.application.AccessToken;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token de acesso para as APIs administrativas")
public record TokenResponse(
		@Schema(description = "Token JWT a ser enviado no header Authorization") String accessToken,
		@Schema(description = "Tipo do token", example = "Bearer") String tokenType,
		@Schema(description = "Validade do token em segundos", example = "3600") long expiresIn) {

	public static TokenResponse from(AccessToken token) {
		return new TokenResponse(token.value(), "Bearer", token.expiresIn().toSeconds());
	}
}
