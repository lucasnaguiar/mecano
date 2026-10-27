package br.dev.lucasaguiar.mecano.auth.domain;

import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Usuário da equipe da oficina com acesso às APIs administrativas.
 */
public class User {

	private final UserId id;
	private final String name;
	private final String email;
	private final String passwordHash;
	private final Role role;
	private final Instant createdAt;
	private final Instant updatedAt;

	private User(UserId id, String name, String email, String passwordHash, Role role, Instant createdAt,
			Instant updatedAt) {
		this.id = Objects.requireNonNull(id, "O identificador do usuário é obrigatório.");
		this.name = requireText(name, "O nome do usuário é obrigatório.");
		this.email = normalizeEmail(requireText(email, "O e-mail do usuário é obrigatório."));
		this.passwordHash = requireText(passwordHash, "A senha do usuário é obrigatória.");
		this.role = Objects.requireNonNull(role, "O perfil do usuário é obrigatório.");
		this.createdAt = Objects.requireNonNull(createdAt, "A data de criação é obrigatória.");
		this.updatedAt = Objects.requireNonNull(updatedAt, "A data de atualização é obrigatória.");
	}

	public static User register(String name, String email, String passwordHash, Role role, Instant now) {
		return new User(UserId.newId(), name, email, passwordHash, role, now, now);
	}

	public static User restore(UserId id, String name, String email, String passwordHash, Role role,
			Instant createdAt, Instant updatedAt) {
		return new User(id, name, email, passwordHash, role, createdAt, updatedAt);
	}

	public static String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}

	private static String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	public UserId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public Role getRole() {
		return role;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
