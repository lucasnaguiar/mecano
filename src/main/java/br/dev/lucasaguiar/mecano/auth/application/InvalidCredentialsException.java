package br.dev.lucasaguiar.mecano.auth.application;

/**
 * E-mail ou senha inválidos.
 */
public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("E-mail ou senha inválidos.");
	}
}
