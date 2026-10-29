package br.dev.lucasaguiar.mecano.inventory.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.inventory.domain.PartType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Mapeamento JPA da tabela part. O lock otimista (version) evita reservas concorrentes da mesma peça.
 */
@Entity
@Table(name = "part")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
class PartEntity {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 50)
	private String code;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private PartType type;

	@Column(name = "unit_of_measure", nullable = false, length = 10)
	private String unitOfMeasure;

	@Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "available_quantity", nullable = false, precision = 12, scale = 3)
	private BigDecimal availableQuantity;

	@Column(name = "reserved_quantity", nullable = false, precision = 12, scale = 3)
	private BigDecimal reservedQuantity;

	@Column(name = "minimum_stock", nullable = false, precision = 12, scale = 3)
	private BigDecimal minimumStock;

	@Column(nullable = false)
	private boolean active;

	@Version
	private Long version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
