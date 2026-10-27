package br.dev.lucasaguiar.mecano.auth.application;

import br.dev.lucasaguiar.mecano.auth.domain.Role;

/**
 * Dados para cadastrar um usuário da equipe.
 */
public record RegisterUserCommand(String name, String email, String password, Role role) {
}
