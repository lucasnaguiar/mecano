package br.dev.lucasaguiar.mecano.auth.application;

import br.dev.lucasaguiar.mecano.auth.domain.User;
import br.dev.lucasaguiar.mecano.auth.domain.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Autentica um usuário ativo da equipe por e-mail e senha e emite um token de acesso. Usuário desativado recebe a
 * mesma resposta de credenciais inválidas, para não revelar quais contas existem.
 */
@Service
public class LoginUseCase {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenProvider tokenProvider;

	public LoginUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenProvider = tokenProvider;
	}

	@Transactional(readOnly = true)
	public AccessToken execute(String email, String password) {
		var user = userRepository.findByEmail(User.normalizeEmail(email))
				.filter(User::isActive)
				.filter(found -> passwordEncoder.matches(password, found.getPasswordHash()))
				.orElseThrow(InvalidCredentialsException::new);
		return tokenProvider.issue(user);
	}
}
