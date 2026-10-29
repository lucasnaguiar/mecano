package br.dev.lucasaguiar.mecano.shared.application;

/**
 * Conflito com dados já existentes (ex.: documento ou placa duplicados). Resulta em 409.
 */
public class ConflictException extends RuntimeException {

	public ConflictException(String message) {
		super(message);
	}
}
