package br.dev.lucasaguiar.mecano.customer.domain.customer;

import br.dev.lucasaguiar.mecano.shared.domain.Page;
import br.dev.lucasaguiar.mecano.shared.domain.PageQuery;
import br.dev.lucasaguiar.mecano.shared.domain.TaxId;
import java.util.Optional;

/**
 * Repositório do agregado Customer.
 */
public interface CustomerRepository {

	Customer save(Customer customer);

	Optional<Customer> findById(CustomerId id);

	Optional<Customer> findByTaxId(TaxId taxId);

	boolean existsByTaxId(TaxId taxId);

	/** Lista os clientes em ordem alfabética. */
	Page<Customer> findAll(PageQuery query);

	void delete(CustomerId id);
}
