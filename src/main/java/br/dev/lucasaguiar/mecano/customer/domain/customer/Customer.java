package br.dev.lucasaguiar.mecano.customer.domain.customer;

import br.dev.lucasaguiar.mecano.customer.domain.customer.event.CustomerRegistered;
import br.dev.lucasaguiar.mecano.shared.domain.AggregateRoot;
import br.dev.lucasaguiar.mecano.shared.domain.Guard;
import br.dev.lucasaguiar.mecano.shared.domain.InvalidValueException;
import br.dev.lucasaguiar.mecano.shared.domain.TaxId;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Aggregate root: cliente da oficina, pessoa física ou jurídica identificada por CPF ou CNPJ.
 */
public class Customer extends AggregateRoot {

	private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
	private static final Pattern PHONE = Pattern.compile("\\d{10,11}");

	private final CustomerId id;
	private final TaxId taxId;
	private final Instant createdAt;
	private String name;
	private String email;
	private String phone;
	private Instant updatedAt;

	private Customer(CustomerId id, String name, TaxId taxId, String email, String phone, Instant createdAt,
			Instant updatedAt) {
		this.id = Objects.requireNonNull(id);
		this.taxId = Guard.requireNonNull(taxId, "O CPF ou CNPJ é obrigatório.");
		this.createdAt = Objects.requireNonNull(createdAt);
		changeData(name, email, phone, updatedAt);
	}

	public static Customer register(String name, TaxId taxId, String email, String phone, Instant now) {
		var customer = new Customer(CustomerId.newId(), name, taxId, email, phone, now, now);
		customer.registerEvent(new CustomerRegistered(customer.id, now));
		return customer;
	}

	public static Customer restore(CustomerId id, String name, TaxId taxId, String email, String phone,
			Instant createdAt, Instant updatedAt) {
		return new Customer(id, name, taxId, email, phone, createdAt, updatedAt);
	}

	public void update(String name, String email, String phone, Instant now) {
		changeData(name, email, phone, now);
	}

	private void changeData(String name, String email, String phone, Instant now) {
		this.name = Guard.requireText(name, "O nome do cliente é obrigatório.");
		this.email = normalizeEmail(email);
		this.phone = normalizePhone(phone);
		this.updatedAt = Objects.requireNonNull(now);
	}

	private static String normalizeEmail(String email) {
		var value = Guard.optionalText(email);
		if (value == null) {
			return null;
		}
		if (!EMAIL.matcher(value).matches()) {
			throw new InvalidValueException("E-mail inválido.");
		}
		return value.toLowerCase(Locale.ROOT);
	}

	private static String normalizePhone(String phone) {
		var value = Guard.optionalText(phone);
		if (value == null) {
			return null;
		}
		var digits = value.replaceAll("\\D", "");
		if (!PHONE.matcher(digits).matches()) {
			throw new InvalidValueException("Telefone inválido. Informe DDD e número (10 ou 11 dígitos).");
		}
		return digits;
	}

	public CustomerId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public TaxId getTaxId() {
		return taxId;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
