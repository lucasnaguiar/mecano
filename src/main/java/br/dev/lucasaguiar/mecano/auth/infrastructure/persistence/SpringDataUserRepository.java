package br.dev.lucasaguiar.mecano.auth.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.auth.domain.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

	Optional<UserEntity> findByEmail(String email);

	boolean existsByEmail(String email);

	List<UserEntity> findAllByOrderByName();

	long countByRoleAndActiveTrue(Role role);
}
