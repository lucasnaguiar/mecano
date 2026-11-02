package br.dev.lucasaguiar.mecano.shared.infrastructure.web;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Limites de requisições por IP nos endpoints públicos (app.rate-limit.*): login e consulta pelo código de
 * acompanhamento.
 */
@ConfigurationProperties("app.rate-limit")
public record RateLimitProperties(Rule login, Rule tracking) {

	public RateLimitProperties {
		if (login == null || tracking == null) {
			throw new IllegalArgumentException("Configure app.rate-limit.login e app.rate-limit.tracking.");
		}
	}

	/** Quantidade máxima de requisições aceitas dentro da janela. */
	public record Rule(int maxRequests, Duration window) {

		public Rule {
			if (maxRequests < 1 || window == null || window.isNegative() || window.isZero()) {
				throw new IllegalArgumentException("O limite de requisições precisa de máximo e janela positivos.");
			}
		}
	}
}
