package br.dev.lucasaguiar.mecano.auth.application;

import br.dev.lucasaguiar.mecano.auth.domain.User;

/**
 * Emissor de tokens de acesso (implementado na infraestrutura com JWT).
 */
public interface TokenProvider {

	AccessToken issue(User user);
}
