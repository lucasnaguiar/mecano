package br.dev.lucasaguiar.mecano.auth.application;

import br.dev.lucasaguiar.mecano.auth.domain.User;
import br.dev.lucasaguiar.mecano.auth.domain.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista os usuários da equipe, ativos e inativos, em ordem alfabética.
 */
@Service
public class ListUsersUseCase {

	private final UserRepository userRepository;

	public ListUsersUseCase(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public List<User> execute() {
		return userRepository.findAll();
	}
}
