package br.dev.lucasaguiar.mecano.customer.domain.vehicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.event.VehicleRegistered;
import br.dev.lucasaguiar.mecano.shared.domain.InvalidValueException;
import br.dev.lucasaguiar.mecano.shared.domain.LicensePlate;
import br.dev.lucasaguiar.mecano.shared.domain.ModelYear;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class VehicleTest {

	private static final Instant NOW = Instant.parse("2026-01-15T12:00:00Z");
	private static final CustomerId OWNER = CustomerId.newId();

	@Test
	void registerRaisesEventAndKnowsItsOwner() {
		var vehicle = Vehicle.register(OWNER, new LicensePlate("BRA2E19"), "Fiat", "Uno", new ModelYear(2015), NOW);

		assertThat(vehicle.belongsTo(OWNER)).isTrue();
		assertThat(vehicle.belongsTo(CustomerId.newId())).isFalse();
		assertThat(vehicle.pullEvents()).singleElement().isInstanceOf(VehicleRegistered.class);
	}

	@Test
	void updateChangesDescriptionOnly() {
		var plate = new LicensePlate("ABC1234");
		var vehicle = Vehicle.restore(VehicleId.newId(), OWNER, plate, "Fiat", "Uno", new ModelYear(2015), NOW, NOW);

		vehicle.update("Fiat", "Mobi", new ModelYear(2020), NOW.plusSeconds(1));

		assertThat(vehicle.getModel()).isEqualTo("Mobi");
		assertThat(vehicle.getModelYear().value()).isEqualTo(2020);
		assertThat(vehicle.getLicensePlate()).isEqualTo(plate);
		assertThat(vehicle.getCustomerId()).isEqualTo(OWNER);
	}

	@Test
	void rejectsMissingData() {
		var plate = new LicensePlate("ABC1234");
		assertThatThrownBy(() -> Vehicle.register(OWNER, plate, " ", "Uno", new ModelYear(2015), NOW))
				.isInstanceOf(InvalidValueException.class).hasMessage("A marca do veículo é obrigatória.");
		assertThatThrownBy(() -> Vehicle.register(null, plate, "Fiat", "Uno", new ModelYear(2015), NOW))
				.isInstanceOf(InvalidValueException.class);
		assertThatThrownBy(() -> Vehicle.register(OWNER, plate, "Fiat", "Uno", null, NOW))
				.isInstanceOf(InvalidValueException.class);
	}
}
