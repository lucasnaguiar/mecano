package br.dev.lucasaguiar.mecano.customer.domain.customer;

import br.dev.lucasaguiar.mecano.shared.domain.TaxId;
import java.util.List;
import java.util.Optional;

/**
 * Repositório do agregado Customer.
 */
public interface CustomerRepository {

	Customer save(Customer customer);

	Optional<Customer> findById(CustomerId id);

	Optional<Customer> findByTaxId(TaxId taxId);

	boolean existsByTaxId(TaxId taxId);

	List<Customer> findAll();

	void delete(CustomerId id);
}
