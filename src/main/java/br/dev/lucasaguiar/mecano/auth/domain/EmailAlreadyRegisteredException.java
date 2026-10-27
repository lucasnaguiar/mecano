package br.dev.lucasaguiar.mecano.auth.domain;

/**
 * Já existe um usuário cadastrado com o e-mail informado.
 */
public class EmailAlreadyRegisteredException extends RuntimeException {

	public EmailAlreadyRegisteredException(String email) {
		super("E-mail já cadastrado: " + email);
	}
}
