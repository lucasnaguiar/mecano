package br.dev.lucasaguiar.mecano.workorder.domain;

/**
 * Status da ordem de serviço.
 */
public enum WorkOrderStatus {
	RECEIVED("Recebida"),
	IN_DIAGNOSIS("Em diagnóstico"),
	AWAITING_APPROVAL("Aguardando aprovação"),
	IN_PROGRESS("Em execução"),
	COMPLETED("Finalizada"),
	DELIVERED("Entregue"),
	CANCELED("Cancelada");

	private final String label;

	WorkOrderStatus(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}
