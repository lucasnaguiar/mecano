package br.dev.lucasaguiar.mecano.customer.application;

import br.dev.lucasaguiar.mecano.customer.domain.customer.Customer;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Altera nome e contatos do cliente. O documento não muda.
 */
@Service
public class UpdateCustomerUseCase {

	private final CustomerRepository customerRepository;
	private final GetCustomerUseCase getCustomerUseCase;

	public UpdateCustomerUseCase(CustomerRepository customerRepository, GetCustomerUseCase getCustomerUseCase) {
		this.customerRepository = customerRepository;
		this.getCustomerUseCase = getCustomerUseCase;
	}

	@Transactional
	public Customer execute(CustomerId id, CustomerData data) {
		var customer = getCustomerUseCase.execute(id);
		customer.update(data.name(), data.email(), data.phone(), Instant.now());
		return customerRepository.save(customer);
	}
}
