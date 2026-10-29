package br.dev.lucasaguiar.mecano.inventory.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.inventory.domain.StockMovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Mapeamento JPA da tabela stock_movement. Fica fora da coleção de PartEntity porque o extrato cresce sem limite;
 * peça, OS e usuário são referenciados apenas pelo ID.
 */
@Entity
@Table(name = "stock_movement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
class StockMovementEntity {

	@Id
	private UUID id;

	@Column(name = "part_id", nullable = false)
	private UUID partId;

	@Column(name = "work_order_id")
	private UUID workOrderId;

	@Column(name = "user_id")
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private StockMovementType type;

	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal quantity;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;
}
