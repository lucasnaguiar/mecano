package br.dev.lucasaguiar.mecano.customer.application;

import br.dev.lucasaguiar.mecano.customer.domain.customer.Customer;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerRepository;
import br.dev.lucasaguiar.mecano.shared.application.ConflictException;
import br.dev.lucasaguiar.mecano.shared.application.DomainEventPublisher;
import br.dev.lucasaguiar.mecano.shared.domain.TaxId;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastra um cliente. O CPF ou CNPJ é único.
 */
@Service
public class RegisterCustomerUseCase {

	private final CustomerRepository customerRepository;
	private final DomainEventPublisher eventPublisher;

	public RegisterCustomerUseCase(CustomerRepository customerRepository, DomainEventPublisher eventPublisher) {
		this.customerRepository = customerRepository;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public Customer execute(String taxId, CustomerData data) {
		var document = new TaxId(taxId);
		if (customerRepository.existsByTaxId(document)) {
			throw new ConflictException("Já existe um cliente com o documento " + document.value() + ".");
		}

		var customer = Customer.register(data.name(), document, data.email(), data.phone(), Instant.now());
		var saved = customerRepository.save(customer);
		eventPublisher.publish(customer.pullEvents());
		return saved;
	}
}
