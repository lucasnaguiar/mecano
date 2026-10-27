package br.dev.lucasaguiar.mecano.auth.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração do JWT (app.security.jwt.*).
 */
@ConfigurationProperties("app.security.jwt")
public record JwtProperties(String secret, Duration expiration) {

	private static final int MIN_SECRET_BYTES = 32;

	public JwtProperties {
		if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
			throw new IllegalArgumentException(
					"JWT_SECRET deve ter pelo menos " + MIN_SECRET_BYTES + " bytes para assinatura HS256.");
		}
		if (expiration == null || expiration.isNegative() || expiration.isZero()) {
			throw new IllegalArgumentException("JWT_EXPIRATION deve ser uma duração positiva (ex.: PT1H).");
		}
	}
}
