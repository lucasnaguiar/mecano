package br.dev.lucasaguiar.mecano.shared.application;

/**
 * Recurso não encontrado. Resulta em 404.
 */
public class NotFoundException extends RuntimeException {

	public NotFoundException(String message) {
		super(message);
	}
}
