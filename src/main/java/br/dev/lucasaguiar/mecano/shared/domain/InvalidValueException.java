package br.dev.lucasaguiar.mecano.shared.domain;

/**
 * Valor informado inválido (formato, faixa ou obrigatoriedade). Resulta em 400.
 */
public class InvalidValueException extends IllegalArgumentException {

	public InvalidValueException(String message) {
		super(message);
	}
}
