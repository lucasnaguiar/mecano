package br.dev.lucasaguiar.mecano.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.dev.lucasaguiar.mecano.auth.domain.EmailAlreadyRegisteredException;
import br.dev.lucasaguiar.mecano.auth.domain.Role;
import br.dev.lucasaguiar.mecano.auth.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private RegisterUserUseCase useCase;

	@Test
	void registersUserWithEncodedPassword() {
		when(userRepository.existsByEmail("joao@mecano.local")).thenReturn(false);
		when(passwordEncoder.encode("senha-forte")).thenReturn("hash");
		when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		var user = useCase.execute(new RegisterUserCommand("João", "Joao@Mecano.local", "senha-forte", Role.MECHANIC));

		assertThat(user.getEmail()).isEqualTo("joao@mecano.local");
		assertThat(user.getPasswordHash()).isEqualTo("hash");
		assertThat(user.getRole()).isEqualTo(Role.MECHANIC);
	}

	@Test
	void rejectsDuplicatedEmail() {
		when(userRepository.existsByEmail("joao@mecano.local")).thenReturn(true);

		assertThatThrownBy(() -> useCase.execute(
				new RegisterUserCommand("João", "joao@mecano.local", "senha-forte", Role.MECHANIC)))
				.isInstanceOf(EmailAlreadyRegisteredException.class);
		verify(userRepository, never()).save(any());
	}
}
