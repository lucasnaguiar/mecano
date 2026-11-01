package br.dev.lucasaguiar.mecano.workorder.domain;

/**
 * Status do orçamento.
 */
public enum EstimateStatus {
	PENDING,
	APPROVED,
	REJECTED,
	/** Encerrado sem resposta do cliente porque a OS foi cancelada pela equipe. */
	CANCELED
}
