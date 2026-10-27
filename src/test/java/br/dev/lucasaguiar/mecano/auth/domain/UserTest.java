package br.dev.lucasaguiar.mecano.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserTest {

	private static final Instant NOW = Instant.parse("2026-10-26T12:00:00Z");

	@Test
	void registerNormalizesEmailAndSetsTimestamps() {
		var user = User.register("Maria", "  Maria@Mecano.LOCAL ", "hash", Role.ATTENDANT, NOW);

		assertThat(user.getId()).isNotNull();
		assertThat(user.getEmail()).isEqualTo("maria@mecano.local");
		assertThat(user.getRole()).isEqualTo(Role.ATTENDANT);
		assertThat(user.getCreatedAt()).isEqualTo(NOW);
		assertThat(user.getUpdatedAt()).isEqualTo(NOW);
	}

	@Test
	void registerRejectsBlankName() {
		assertThatThrownBy(() -> User.register(" ", "a@b.com", "hash", Role.ADMIN, NOW))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("O nome do usuário é obrigatório.");
	}

	@Test
	void registerRejectsMissingRole() {
		assertThatThrownBy(() -> User.register("Maria", "a@b.com", "hash", null, NOW))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	void restoreKeepsGivenIdentity() {
		var id = UserId.newId();
		var user = User.restore(id, "Maria", "a@b.com", "hash", Role.MECHANIC, NOW, NOW);

		assertThat(user.getId()).isEqualTo(id);
	}
}
