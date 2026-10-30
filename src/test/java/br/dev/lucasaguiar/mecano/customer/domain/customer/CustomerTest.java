package br.dev.lucasaguiar.mecano.customer.domain.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.dev.lucasaguiar.mecano.customer.domain.customer.event.CustomerRegistered;
import br.dev.lucasaguiar.mecano.shared.domain.InvalidValueException;
import br.dev.lucasaguiar.mecano.shared.domain.TaxId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CustomerTest {

	private static final Instant NOW = Instant.parse("2026-01-15T12:00:00Z");
	private static final TaxId CPF = new TaxId("52998224725");

	@Test
	void registerNormalizesContactsAndRaisesEvent() {
		var customer = Customer.register(" Ana Souza ", CPF, " Ana@Email.COM ", "(11) 99999-0000", NOW);

		assertThat(customer.getName()).isEqualTo("Ana Souza");
		assertThat(customer.getEmail()).isEqualTo("ana@email.com");
		assertThat(customer.getPhone()).isEqualTo("11999990000");
		assertThat(customer.pullEvents()).singleElement().isInstanceOf(CustomerRegistered.class);
		assertThat(customer.pullEvents()).isEmpty();
	}

	@Test
	void contactsAreOptional() {
		var customer = Customer.register("Ana", CPF, " ", null, NOW);

		assertThat(customer.getEmail()).isNull();
		assertThat(customer.getPhone()).isNull();
	}

	@Test
	void rejectsInvalidData() {
		assertThatThrownBy(() -> Customer.register(" ", CPF, null, null, NOW))
				.isInstanceOf(InvalidValueException.class).hasMessage("O nome do cliente é obrigatório.");
		assertThatThrownBy(() -> Customer.register("Ana", CPF, "ana@", null, NOW))
				.isInstanceOf(InvalidValueException.class).hasMessage("E-mail inválido.");
		assertThatThrownBy(() -> Customer.register("Ana", CPF, null, "9999", NOW))
				.isInstanceOf(InvalidValueException.class);
		assertThatThrownBy(() -> Customer.register("Ana", null, null, null, NOW))
				.isInstanceOf(InvalidValueException.class);
	}

	@Test
	void updateChangesDataButKeepsDocument() {
		var customer = Customer.restore(CustomerId.newId(), "Ana", CPF, null, null, NOW, NOW);
		var later = NOW.plusSeconds(60);

		customer.update("Ana Lima", "ana@lima.com", "1133334444", later);

		assertThat(customer.getName()).isEqualTo("Ana Lima");
		assertThat(customer.getTaxId()).isEqualTo(CPF);
		assertThat(customer.getUpdatedAt()).isEqualTo(later);
		assertThat(customer.getCreatedAt()).isEqualTo(NOW);
		assertThat(customer.pullEvents()).isEmpty();
	}
}
