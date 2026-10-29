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
 * Mapeamento JPA da tabela part_item. Descrição, unidade e preço são cópias do estoque no momento da inclusão.
 */
@Entity
@Table(name = "part_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
class PartItemEntity {

	@Id
	private UUID id;

	@Column(name = "part_id", nullable = false)
	private UUID partId;

	@Column(nullable = false)
	private String description;

	@Column(name = "unit_of_measure", nullable = false, length = 10)
	private String unitOfMeasure;

	@Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
	private BigDecimal unitPrice;

	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal quantity;
}
