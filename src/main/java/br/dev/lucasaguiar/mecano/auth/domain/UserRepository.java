package br.dev.lucasaguiar.mecano.auth.domain;

import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import java.util.List;
import java.util.Optional;

/**
 * Repositório de User.
 */
public interface UserRepository {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	User save(User user);

	Optional<User> findById(UserId id);

	List<User> findAll();

	long countActiveAdmins();
}
