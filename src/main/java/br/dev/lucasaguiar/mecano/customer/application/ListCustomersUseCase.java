package br.dev.lucasaguiar.mecano.customer.application;

import br.dev.lucasaguiar.mecano.customer.domain.customer.Customer;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerRepository;
import br.dev.lucasaguiar.mecano.shared.domain.Page;
import br.dev.lucasaguiar.mecano.shared.domain.PageQuery;
import br.dev.lucasaguiar.mecano.shared.domain.TaxId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista os clientes em ordem alfabética, paginados, ou busca pelo CPF/CNPJ, quando informado.
 */
@Service
public class ListCustomersUseCase {

	private final CustomerRepository customerRepository;

	public ListCustomersUseCase(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Transactional(readOnly = true)
	public Page<Customer> execute(String taxId, PageQuery query) {
		if (taxId == null || taxId.isBlank()) {
			return customerRepository.findAll(query);
		}
		List<Customer> found = customerRepository.findByTaxId(new TaxId(taxId)).stream().toList();
		return new Page<>(found, query.page(), query.size(), found.size());
	}
}
