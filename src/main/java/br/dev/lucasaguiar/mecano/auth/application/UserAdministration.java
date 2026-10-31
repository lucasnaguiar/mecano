package br.dev.lucasaguiar.mecano.auth.application;

import br.dev.lucasaguiar.mecano.auth.domain.User;
import br.dev.lucasaguiar.mecano.auth.domain.UserRepository;
import br.dev.lucasaguiar.mecano.shared.application.NotFoundException;
import br.dev.lucasaguiar.mecano.shared.domain.BusinessRuleException;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import org.springframework.stereotype.Component;

/**
 * Regras comuns da gestão de usuários: ninguém altera o próprio perfil ou se desativa, e a oficina nunca fica sem
 * um administrador ativo.
 */
@Component
class UserAdministration {

	private final UserRepository userRepository;

	UserAdministration(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	User loadOtherUser(UserId id, UserId requestedBy) {
		if (id.equals(requestedBy)) {
			throw new BusinessRuleException("Você não pode alterar o perfil nem desativar o próprio usuário.");
		}
		return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
	}

	void ensureAnotherActiveAdminRemains(User user) {
		if (user.isActiveAdmin() && userRepository.countActiveAdmins() <= 1) {
			throw new BusinessRuleException("A oficina precisa de ao menos um administrador ativo.");
		}
	}
}
