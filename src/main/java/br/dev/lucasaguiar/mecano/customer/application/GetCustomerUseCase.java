package br.dev.lucasaguiar.mecano.customer.application;

import br.dev.lucasaguiar.mecano.customer.domain.customer.Customer;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerRepository;
import br.dev.lucasaguiar.mecano.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta um cliente pelo ID.
 */
@Service
public class GetCustomerUseCase {

	private final CustomerRepository customerRepository;

	public GetCustomerUseCase(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Transactional(readOnly = true)
	public Customer execute(CustomerId id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Cliente não encontrado."));
	}
}
