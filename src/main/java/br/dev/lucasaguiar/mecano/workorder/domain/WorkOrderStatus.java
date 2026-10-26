package br.dev.lucasaguiar.mecano.workorder.domain;

/**
 * Status da ordem de serviço.
 */
public enum WorkOrderStatus {
	RECEIVED,
	IN_DIAGNOSIS,
	AWAITING_APPROVAL,
	IN_PROGRESS,
	COMPLETED,
	DELIVERED
}
