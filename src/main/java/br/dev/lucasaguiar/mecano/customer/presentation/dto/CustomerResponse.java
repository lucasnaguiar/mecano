package br.dev.lucasaguiar.mecano.customer.presentation.dto;

import br.dev.lucasaguiar.mecano.customer.domain.customer.Customer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Cliente da oficina")
public record CustomerResponse(UUID id, String name, String taxId, String email, String phone, Instant createdAt,
		Instant updatedAt) {

	public static CustomerResponse from(Customer customer) {
		return new CustomerResponse(customer.getId().value(), customer.getName(), customer.getTaxId().value(),
				customer.getEmail(), customer.getPhone(), customer.getCreatedAt(), customer.getUpdatedAt());
	}
}
