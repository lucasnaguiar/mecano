package br.dev.lucasaguiar.mecano.customer.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.customer.domain.customer.Customer;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerRepository;
import br.dev.lucasaguiar.mecano.shared.domain.TaxId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/**
 * Implementação JPA de CustomerRepository.
 */
@Repository
public class JpaCustomerRepository implements CustomerRepository {

	private final SpringDataCustomerRepository springDataRepository;

	public JpaCustomerRepository(SpringDataCustomerRepository springDataRepository) {
		this.springDataRepository = springDataRepository;
	}

	@Override
	public Customer save(Customer customer) {
		return toDomain(springDataRepository.save(toEntity(customer)));
	}

	@Override
	public Optional<Customer> findById(CustomerId id) {
		return springDataRepository.findById(id.value()).map(JpaCustomerRepository::toDomain);
	}

	@Override
	public Optional<Customer> findByTaxId(TaxId taxId) {
		return springDataRepository.findByTaxId(taxId.value()).map(JpaCustomerRepository::toDomain);
	}

	@Override
	public boolean existsByTaxId(TaxId taxId) {
		return springDataRepository.existsByTaxId(taxId.value());
	}

	@Override
	public List<Customer> findAll() {
		return springDataRepository.findAll(Sort.by("name")).stream().map(JpaCustomerRepository::toDomain).toList();
	}

	@Override
	public void delete(CustomerId id) {
		springDataRepository.deleteById(id.value());
		springDataRepository.flush();
	}

	private static CustomerEntity toEntity(Customer customer) {
		return new CustomerEntity(customer.getId().value(), customer.getName(), customer.getTaxId().value(),
				customer.getEmail(), customer.getPhone(), customer.getCreatedAt(), customer.getUpdatedAt());
	}

	private static Customer toDomain(CustomerEntity entity) {
		return Customer.restore(new CustomerId(entity.getId()), entity.getName(), new TaxId(entity.getTaxId()),
				entity.getEmail(), entity.getPhone(), entity.getCreatedAt(), entity.getUpdatedAt());
	}
}
