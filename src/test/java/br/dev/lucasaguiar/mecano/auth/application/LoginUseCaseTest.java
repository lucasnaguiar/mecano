package br.dev.lucasaguiar.mecano.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.dev.lucasaguiar.mecano.auth.domain.Role;
import br.dev.lucasaguiar.mecano.auth.domain.User;
import br.dev.lucasaguiar.mecano.auth.domain.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

	private static final User USER = User.register("Maria", "maria@mecano.local", "hash", Role.ADMIN, Instant.now());

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private TokenProvider tokenProvider;

	@InjectMocks
	private LoginUseCase useCase;

	@Test
	void issuesTokenForValidCredentials() {
		var token = new AccessToken("jwt", Duration.ofHours(1));
		when(userRepository.findByEmail("maria@mecano.local")).thenReturn(Optional.of(USER));
		when(passwordEncoder.matches("senha", "hash")).thenReturn(true);
		when(tokenProvider.issue(USER)).thenReturn(token);

		assertThat(useCase.execute(" MARIA@mecano.local", "senha")).isEqualTo(token);
	}

	@Test
	void rejectsWrongPassword() {
		when(userRepository.findByEmail("maria@mecano.local")).thenReturn(Optional.of(USER));
		when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

		assertThatThrownBy(() -> useCase.execute("maria@mecano.local", "errada"))
				.isInstanceOf(InvalidCredentialsException.class);
		verify(tokenProvider, never()).issue(any());
	}

	@Test
	void rejectsUnknownEmail() {
		when(userRepository.findByEmail("ninguem@mecano.local")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute("ninguem@mecano.local", "senha"))
				.isInstanceOf(InvalidCredentialsException.class);
	}
}
