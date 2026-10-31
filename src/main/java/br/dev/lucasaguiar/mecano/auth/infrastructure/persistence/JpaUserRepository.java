package br.dev.lucasaguiar.mecano.auth.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.auth.domain.Role;
import br.dev.lucasaguiar.mecano.auth.domain.User;
import br.dev.lucasaguiar.mecano.auth.domain.UserRepository;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import java.util.List;
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
	public Optional<User> findById(UserId id) {
		return springDataRepository.findById(id.value()).map(JpaUserRepository::toDomain);
	}

	@Override
	public List<User> findAll() {
		return springDataRepository.findAllByOrderByName().stream().map(JpaUserRepository::toDomain).toList();
	}

	@Override
	public long countActiveAdmins() {
		return springDataRepository.countByRoleAndActiveTrue(Role.ADMIN);
	}

	@Override
	public User save(User user) {
		return toDomain(springDataRepository.save(toEntity(user)));
	}

	private static UserEntity toEntity(User user) {
		return new UserEntity(user.getId().value(), user.getName(), user.getEmail(), user.getPasswordHash(),
				user.getRole(), user.isActive(), user.getCreatedAt(), user.getUpdatedAt());
	}

	private static User toDomain(UserEntity entity) {
		return User.restore(new UserId(entity.getId()), entity.getName(), entity.getEmail(),
				entity.getPasswordHash(), entity.getRole(), entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt());
	}
}
