package br.dev.lucasaguiar.mecano.customer.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import br.dev.lucasaguiar.mecano.PersistenceTest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@PersistenceTest
class CustomerPersistenceTest {

	private static final Instant NOW = Instant.parse("2026-01-15T12:00:00Z");

	@Autowired
	private SpringDataCustomerRepository customers;

	@Autowired
	private SpringDataVehicleRepository vehicles;

	@Test
	void findsCustomerByTaxId() {
		var customer = customers.save(customer("12345678901"));

		assertThat(customers.findByTaxId("12345678901")).get().extracting(CustomerEntity::getId).isEqualTo(customer.getId());
		assertThat(customers.existsByTaxId("98765432100")).isFalse();
	}

	@Test
	void findsVehiclesByCustomerAndPlate() {
		var customer = customers.save(customer("12ABC34501DE35"));
		vehicles.save(vehicle(customer.getId(), "ABC1D23"));
		vehicles.save(vehicle(customer.getId(), "XYZ1234"));

		assertThat(vehicles.findByCustomerId(customer.getId())).hasSize(2);
		assertThat(vehicles.findByLicensePlate("ABC1D23")).isPresent();
		assertThat(vehicles.existsByLicensePlate("AAA0000")).isFalse();
	}

	private static CustomerEntity customer(String taxId) {
		return new CustomerEntity(UUID.randomUUID(), "Ana Souza", taxId, "ana@email.com", "11999990000", NOW, NOW);
	}

	private static VehicleEntity vehicle(UUID customerId, String plate) {
		return new VehicleEntity(UUID.randomUUID(), customerId, plate, "Fiat", "Uno", (short) 2015, NOW, NOW);
	}
}
