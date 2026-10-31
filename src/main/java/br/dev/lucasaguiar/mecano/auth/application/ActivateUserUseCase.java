package br.dev.lucasaguiar.mecano.auth.application;

import br.dev.lucasaguiar.mecano.auth.domain.User;
import br.dev.lucasaguiar.mecano.auth.domain.UserRepository;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reativa um usuário desativado.
 */
@Service
public class ActivateUserUseCase {

	private final UserRepository userRepository;
	private final UserAdministration userAdministration;

	public ActivateUserUseCase(UserRepository userRepository, UserAdministration userAdministration) {
		this.userRepository = userRepository;
		this.userAdministration = userAdministration;
	}

	@Transactional
	public User execute(UserId id, UserId requestedBy) {
		var user = userAdministration.loadOtherUser(id, requestedBy);
		user.activate(Instant.now());
		return userRepository.save(user);
	}
}
