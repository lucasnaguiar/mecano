package br.dev.lucasaguiar.mecano.auth.application;

import br.dev.lucasaguiar.mecano.auth.domain.Role;
import br.dev.lucasaguiar.mecano.auth.domain.User;
import br.dev.lucasaguiar.mecano.auth.domain.UserRepository;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Altera o perfil de acesso de outro usuário. O novo perfil vale a partir do próximo login.
 */
@Service
public class ChangeUserRoleUseCase {

	private final UserRepository userRepository;
	private final UserAdministration userAdministration;

	public ChangeUserRoleUseCase(UserRepository userRepository, UserAdministration userAdministration) {
		this.userRepository = userRepository;
		this.userAdministration = userAdministration;
	}

	@Transactional
	public User execute(UserId id, Role role, UserId requestedBy) {
		var user = userAdministration.loadOtherUser(id, requestedBy);
		if (role != Role.ADMIN) {
			userAdministration.ensureAnotherActiveAdminRemains(user);
		}
		user.changeRole(role, Instant.now());
		return userRepository.save(user);
	}
}
