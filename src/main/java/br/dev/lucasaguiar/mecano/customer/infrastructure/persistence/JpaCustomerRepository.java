package br.dev.lucasaguiar.mecano.customer.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerRepository;
import org.springframework.stereotype.Repository;

/**
 * Implementação JPA de CustomerRepository.
 */
@Repository
public class JpaCustomerRepository implements CustomerRepository {
}
