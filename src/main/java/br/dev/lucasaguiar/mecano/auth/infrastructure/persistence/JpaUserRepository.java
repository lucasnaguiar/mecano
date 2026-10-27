package br.dev.lucasaguiar.mecano.auth.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.auth.domain.User;
import br.dev.lucasaguiar.mecano.auth.domain.UserId;
import br.dev.lucasaguiar.mecano.auth.domain.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Implementação JPA de UserRepository.
 */
@Repository
public class JpaUserRepository implements UserRepository {

	private final SpringDataUserRepository springDataRepository;

	public JpaUserRepository(SpringDataUserRepository springDataRepository) {
		this.springDataRepository = springDataRepository;
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return springDataRepository.findByEmail(email).map(JpaUserRepository::toDomain);
	}

	@Override
	public boolean existsByEmail(String email) {
		return springDataRepository.existsByEmail(email);
	}

	@Override
	public User save(User user) {
		return toDomain(springDataRepository.save(toEntity(user)));
	}

	private static UserEntity toEntity(User user) {
		return new UserEntity(user.getId().value(), user.getName(), user.getEmail(), user.getPasswordHash(),
				user.getRole(), user.getCreatedAt(), user.getUpdatedAt());
	}

	private static User toDomain(UserEntity entity) {
		return User.restore(new UserId(entity.getId()), entity.getName(), entity.getEmail(),
				entity.getPasswordHash(), entity.getRole(), entity.getCreatedAt(), entity.getUpdatedAt());
	}
}
