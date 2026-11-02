package br.dev.lucasaguiar.mecano.shared.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class FixedWindowRateLimiterTest {

	private static final Instant NOW = Instant.parse("2026-01-15T12:00:00Z");

	private Instant now = NOW;
	private final Clock clock = new Clock() {
		@Override
		public ZoneOffset getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(java.time.ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	};

	private final FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(
			new RateLimitProperties.Rule(2, Duration.ofMinutes(1)), clock);

	@Test
	void acceptsUpToTheLimitAndReportsWhenTheWindowReopens() {
		assertThat(limiter.tryAcquire("10.0.0.1")).isZero();
		assertThat(limiter.tryAcquire("10.0.0.1")).isZero();

		now = NOW.plusSeconds(15);
		assertThat(limiter.tryAcquire("10.0.0.1")).isEqualTo(Duration.ofSeconds(45));
		assertThat(limiter.tryAcquire("10.0.0.2")).isZero();

		now = NOW.plusSeconds(60);
		assertThat(limiter.tryAcquire("10.0.0.1")).isZero();
	}

	@Test
	void rejectsInvalidRules() {
		assertThatThrownBy(() -> new RateLimitProperties.Rule(0, Duration.ofMinutes(1)))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new RateLimitProperties.Rule(1, Duration.ZERO))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new RateLimitProperties(null, null)).isInstanceOf(IllegalArgumentException.class);
	}
}
