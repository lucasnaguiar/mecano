package br.dev.lucasaguiar.mecano.shared.domain;

/**
 * Operação que viola uma regra de negócio (ex.: aprovar orçamento sem estoque). Resulta em 422.
 */
public class BusinessRuleException extends RuntimeException {

	public BusinessRuleException(String message) {
		super(message);
	}
}
