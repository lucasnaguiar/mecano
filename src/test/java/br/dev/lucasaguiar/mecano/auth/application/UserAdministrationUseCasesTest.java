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
import br.dev.lucasaguiar.mecano.shared.application.NotFoundException;
import br.dev.lucasaguiar.mecano.shared.domain.BusinessRuleException;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAdministrationUseCasesTest {

	private static final UserId REQUESTER = UserId.newId();

	@Mock
	private UserRepository userRepository;

	private ChangeUserRoleUseCase changeRole;
	private DeactivateUserUseCase deactivate;
	private ActivateUserUseCase activate;

	@BeforeEach
	void setUp() {
		var administration = new UserAdministration(userRepository);
		changeRole = new ChangeUserRoleUseCase(userRepository, administration);
		deactivate = new DeactivateUserUseCase(userRepository, administration);
		activate = new ActivateUserUseCase(userRepository, administration);
	}

	@Test
	void changesRoleOfAnotherUser() {
		var mechanic = user(Role.MECHANIC);
		when(userRepository.findById(mechanic.getId())).thenReturn(Optional.of(mechanic));
		when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		assertThat(changeRole.execute(mechanic.getId(), Role.ATTENDANT, REQUESTER).getRole()).isEqualTo(Role.ATTENDANT);
	}

	@Test
	void cannotChangeOwnRoleOrDeactivateSelf() {
		assertThatThrownBy(() -> changeRole.execute(REQUESTER, Role.MECHANIC, REQUESTER))
				.isInstanceOf(BusinessRuleException.class)
				.hasMessage("Você não pode alterar o perfil nem desativar o próprio usuário.");
		assertThatThrownBy(() -> deactivate.execute(REQUESTER, REQUESTER)).isInstanceOf(BusinessRuleException.class);
		verify(userRepository, never()).save(any());
	}

	@Test
	void keepsAtLeastOneActiveAdmin() {
		var lastAdmin = user(Role.ADMIN);
		when(userRepository.findById(lastAdmin.getId())).thenReturn(Optional.of(lastAdmin));
		when(userRepository.countActiveAdmins()).thenReturn(1L);

		assertThatThrownBy(() -> deactivate.execute(lastAdmin.getId(), REQUESTER))
				.isInstanceOf(BusinessRuleException.class)
				.hasMessage("A oficina precisa de ao menos um administrador ativo.");
		assertThatThrownBy(() -> changeRole.execute(lastAdmin.getId(), Role.MECHANIC, REQUESTER))
				.isInstanceOf(BusinessRuleException.class);
		verify(userRepository, never()).save(any());
	}

	@Test
	void deactivatesAdminWhenAnotherActiveAdminRemains() {
		var admin = user(Role.ADMIN);
		when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
		when(userRepository.countActiveAdmins()).thenReturn(2L);
		when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		assertThat(deactivate.execute(admin.getId(), REQUESTER).isActive()).isFalse();
	}

	@Test
	void reactivatesUser() {
		var inactive = user(Role.MECHANIC);
		inactive.deactivate(Instant.now());
		when(userRepository.findById(inactive.getId())).thenReturn(Optional.of(inactive));
		when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		assertThat(activate.execute(inactive.getId(), REQUESTER).isActive()).isTrue();
	}

	@Test
	void failsForUnknownUser() {
		var unknown = UserId.newId();
		when(userRepository.findById(unknown)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> activate.execute(unknown, REQUESTER)).isInstanceOf(NotFoundException.class);
	}

	@Test
	void listsUsers() {
		var users = List.of(user(Role.ADMIN), user(Role.MECHANIC));
		when(userRepository.findAll()).thenReturn(users);

		assertThat(new ListUsersUseCase(userRepository).execute()).isEqualTo(users);
	}

	private static User user(Role role) {
		return User.register("Usuário", role.name().toLowerCase() + "@mecano.local", "hash", role, Instant.now());
	}
}
