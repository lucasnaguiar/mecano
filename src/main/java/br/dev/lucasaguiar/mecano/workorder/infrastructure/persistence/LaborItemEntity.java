package br.dev.lucasaguiar.mecano.workorder.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Mapeamento JPA da tabela labor_item. Descrição e preço são cópias do catálogo no momento da inclusão.
 */
@Entity
@Table(name = "labor_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
class LaborItemEntity {

	@Id
	private UUID id;

	@Column(name = "labor_operation_id", nullable = false)
	private UUID laborOperationId;

	@Column(nullable = false)
	private String description;

	@Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
	private BigDecimal unitPrice;

	@Column(nullable = false)
	private Integer quantity;
}
