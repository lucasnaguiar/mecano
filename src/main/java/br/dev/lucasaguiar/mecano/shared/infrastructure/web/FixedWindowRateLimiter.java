package br.dev.lucasaguiar.mecano.shared.infrastructure.web;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitador em memória por janela fixa: cada chave (IP) pode fazer até maxRequests dentro da janela. Suficiente para
 * uma instância só; com várias instâncias o contador passaria para um armazenamento compartilhado (ex.: Redis).
 */
class FixedWindowRateLimiter {

	/** Acima disso, janelas vencidas são descartadas para o mapa não crescer sem limite. */
	private static final int CLEANUP_THRESHOLD = 10_000;

	private final RateLimitProperties.Rule rule;
	private final Clock clock;
	private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

	FixedWindowRateLimiter(RateLimitProperties.Rule rule, Clock clock) {
		this.rule = rule;
		this.clock = clock;
	}

	/** Registra a requisição e devolve zero se ela é aceita, ou quanto falta para a janela reabrir. */
	Duration tryAcquire(String key) {
		var now = clock.instant();
		if (windows.size() > CLEANUP_THRESHOLD) {
			windows.values().removeIf(window -> window.expired(now, rule.window()));
		}
		var window = windows.compute(key, (ignored, current) -> current == null || current.expired(now, rule.window())
				? new Window(now, 1)
				: new Window(current.start(), current.count() + 1));
		if (window.count() <= rule.maxRequests()) {
			return Duration.ZERO;
		}
		return Duration.between(now, window.start().plus(rule.window()));
	}

	private record Window(Instant start, int count) {

		boolean expired(Instant now, Duration length) {
			return !now.isBefore(start.plus(length));
		}
	}
}
