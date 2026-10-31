package br.dev.lucasaguiar.mecano.auth.application;

import br.dev.lucasaguiar.mecano.auth.domain.User;
import br.dev.lucasaguiar.mecano.auth.domain.UserRepository;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Desativa outro usuário: ele deixa de conseguir fazer login, mas continua no histórico das OS e do estoque.
 */
@Service
public class DeactivateUserUseCase {

	private final UserRepository userRepository;
	private final UserAdministration userAdministration;

	public DeactivateUserUseCase(UserRepository userRepository, UserAdministration userAdministration) {
		this.userRepository = userRepository;
		this.userAdministration = userAdministration;
	}

	@Transactional
	public User execute(UserId id, UserId requestedBy) {
		var user = userAdministration.loadOtherUser(id, requestedBy);
		userAdministration.ensureAnotherActiveAdminRemains(user);
		user.deactivate(Instant.now());
		return userRepository.save(user);
	}
}
