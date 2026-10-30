package br.dev.lucasaguiar.mecano.customer.domain.vehicle;

import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.event.VehicleRegistered;
import br.dev.lucasaguiar.mecano.shared.domain.AggregateRoot;
import br.dev.lucasaguiar.mecano.shared.domain.Guard;
import br.dev.lucasaguiar.mecano.shared.domain.LicensePlate;
import br.dev.lucasaguiar.mecano.shared.domain.ModelYear;
import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root: veículo atendido pela oficina. Pertence a um cliente, referenciado apenas pelo ID.
 */
public class Vehicle extends AggregateRoot {

	private final VehicleId id;
	private final CustomerId customerId;
	private final LicensePlate licensePlate;
	private final Instant createdAt;
	private String brand;
	private String model;
	private ModelYear modelYear;
	private Instant updatedAt;

	private Vehicle(VehicleId id, CustomerId customerId, LicensePlate licensePlate, String brand, String model,
			ModelYear modelYear, Instant createdAt, Instant updatedAt) {
		this.id = Objects.requireNonNull(id);
		this.customerId = Guard.requireNonNull(customerId, "O cliente do veículo é obrigatório.");
		this.licensePlate = Guard.requireNonNull(licensePlate, "A placa é obrigatória.");
		this.createdAt = Objects.requireNonNull(createdAt);
		changeData(brand, model, modelYear, updatedAt);
	}

	public static Vehicle register(CustomerId customerId, LicensePlate licensePlate, String brand, String model,
			ModelYear modelYear, Instant now) {
		var vehicle = new Vehicle(VehicleId.newId(), customerId, licensePlate, brand, model, modelYear, now, now);
		vehicle.registerEvent(new VehicleRegistered(vehicle.id, now));
		return vehicle;
	}

	public static Vehicle restore(VehicleId id, CustomerId customerId, LicensePlate licensePlate, String brand,
			String model, ModelYear modelYear, Instant createdAt, Instant updatedAt) {
		return new Vehicle(id, customerId, licensePlate, brand, model, modelYear, createdAt, updatedAt);
	}

	public void update(String brand, String model, ModelYear modelYear, Instant now) {
		changeData(brand, model, modelYear, now);
	}

	public boolean belongsTo(CustomerId customerId) {
		return this.customerId.equals(customerId);
	}

	private void changeData(String brand, String model, ModelYear modelYear, Instant now) {
		this.brand = Guard.requireText(brand, "A marca do veículo é obrigatória.");
		this.model = Guard.requireText(model, "O modelo do veículo é obrigatório.");
		this.modelYear = Guard.requireNonNull(modelYear, "O ano do modelo é obrigatório.");
		this.updatedAt = Objects.requireNonNull(now);
	}

	public VehicleId getId() {
		return id;
	}

	public CustomerId getCustomerId() {
		return customerId;
	}

	public LicensePlate getLicensePlate() {
		return licensePlate;
	}

	public String getBrand() {
		return brand;
	}

	public String getModel() {
		return model;
	}

	public ModelYear getModelYear() {
		return modelYear;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
