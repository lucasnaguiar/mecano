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
 * Mapeamento JPA da tabela customer.
 */
@Entity
@Table(name = "customer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
class CustomerEntity {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(name = "tax_id", nullable = false, unique = true, length = 14)
	private String taxId;

	private String email;

	@Column(length = 20)
	private String phone;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
