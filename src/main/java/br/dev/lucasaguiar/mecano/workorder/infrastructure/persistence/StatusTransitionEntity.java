package br.dev.lucasaguiar.mecano.workorder.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Mapeamento JPA da tabela status_transition. changedBy nulo indica ação do cliente ou do sistema.
 */
@Entity
@Table(name = "status_transition")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
class StatusTransitionEntity {

	@Id
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_status", length = 32)
	private WorkOrderStatus fromStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_status", nullable = false, length = 32)
	private WorkOrderStatus toStatus;

	@Column(name = "changed_by")
	private UUID changedBy;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;
}
