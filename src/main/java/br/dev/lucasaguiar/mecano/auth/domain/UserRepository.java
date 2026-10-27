package br.dev.lucasaguiar.mecano.auth.domain;

import java.util.Optional;

/**
 * Repositório de User.
 */
public interface UserRepository {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	User save(User user);
}
