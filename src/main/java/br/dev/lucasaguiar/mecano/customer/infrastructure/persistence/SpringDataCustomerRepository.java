package br.dev.lucasaguiar.mecano.customer.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity, UUID> {

	Optional<CustomerEntity> findByTaxId(String taxId);

	boolean existsByTaxId(String taxId);
}
