package br.dev.lucasaguiar.mecano.customer.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Mapeamento JPA da tabela vehicle. O cliente é referenciado apenas pelo ID (agregados separados).
 */
@Entity
@Table(name = "vehicle")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
class VehicleEntity {

	@Id
	private UUID id;

	@Column(name = "customer_id", nullable = false)
	private UUID customerId;

	@Column(name = "license_plate", nullable = false, unique = true, length = 7)
	private String licensePlate;

	@Column(nullable = false, length = 100)
	private String brand;

	@Column(nullable = false, length = 100)
	private String model;

	@Column(name = "model_year", nullable = false)
	private Short modelYear;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
