package br.dev.lucasaguiar.mecano.auth.application;

import br.dev.lucasaguiar.mecano.auth.domain.EmailAlreadyRegisteredException;
import br.dev.lucasaguiar.mecano.auth.domain.User;
import br.dev.lucasaguiar.mecano.auth.domain.UserRepository;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastra um usuário da equipe da oficina.
 */
@Service
public class RegisterUserUseCase {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public RegisterUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public User execute(RegisterUserCommand command) {
		var email = User.normalizeEmail(command.email());
		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyRegisteredException(email);
		}

		var user = User.register(command.name(), email, passwordEncoder.encode(command.password()),
				command.role(), Instant.now());
		return userRepository.save(user);
	}
}
