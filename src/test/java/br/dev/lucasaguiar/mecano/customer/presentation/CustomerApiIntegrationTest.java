package br.dev.lucasaguiar.mecano.customer.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.dev.lucasaguiar.mecano.ApiIntegrationTest;
import br.dev.lucasaguiar.mecano.TestData;
import org.junit.jupiter.api.Test;

class CustomerApiIntegrationTest extends ApiIntegrationTest {

	@Test
	void managesCustomerAndVehicles() throws Exception {
		var cpf = TestData.randomCpf();
		var formattedCpf = "%s.%s.%s-%s".formatted(cpf.substring(0, 3), cpf.substring(3, 6), cpf.substring(6, 9),
				cpf.substring(9));
		var created = perform(post("/api/admin/customers"), """
				{"name": "Ana Souza", "taxId": "%s", "email": "Ana@Email.com", "phone": "(11) 99999-0000"}
				""".formatted(formattedCpf))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.taxId").value(cpf))
				.andExpect(jsonPath("$.email").value("ana@email.com"));
		String customerId = read(created, "$.id");

		perform(post("/api/admin/customers"), """
				{"name": "Outra Ana", "taxId": "%s"}
				""".formatted(cpf))
				.andExpect(status().isConflict());

		perform(get("/api/admin/customers").param("taxId", formattedCpf))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(customerId));

		perform(put("/api/admin/customers/" + customerId), """
				{"name": "Ana Lima", "email": null, "phone": "1133334444"}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Ana Lima"));

		var plate = TestData.randomPlate();
		var vehicle = perform(post("/api/admin/vehicles"), """
				{"customerId": "%s", "licensePlate": "%s-%s", "brand": "Fiat", "model": "Uno", "modelYear": 2015}
				""".formatted(customerId, plate.substring(0, 3), plate.substring(3)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.licensePlate").value(plate));
		String vehicleId = read(vehicle, "$.id");

		perform(post("/api/admin/vehicles"), """
				{"customerId": "%s", "licensePlate": "%s", "brand": "VW", "model": "Gol", "modelYear": 2010}
				""".formatted(customerId, plate))
				.andExpect(status().isConflict());

		perform(get("/api/admin/vehicles").param("licensePlate", plate.toLowerCase()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(vehicleId));
		perform(get("/api/admin/customers/" + customerId + "/vehicles"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		perform(delete("/api/admin/customers/" + customerId)).andExpect(status().isConflict());
		perform(delete("/api/admin/vehicles/" + vehicleId)).andExpect(status().isNoContent());
		perform(delete("/api/admin/customers/" + customerId)).andExpect(status().isNoContent());
		perform(get("/api/admin/customers/" + customerId)).andExpect(status().isNotFound());
	}

	@Test
	void rejectsInvalidInput() throws Exception {
		perform(post("/api/admin/customers"), """
				{"name": "Ana", "taxId": "123.456.789-00"}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("CPF ou CNPJ inválido."));

		perform(post("/api/admin/customers"), """
				{"name": "", "taxId": ""}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.name").value("O nome é obrigatório."));

		perform(post("/api/admin/vehicles"), """
				{"customerId": "00000000-0000-0000-0000-000000000000", "licensePlate": "ABC1234",
				 "brand": "Fiat", "model": "Uno", "modelYear": 2015}
				""")
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Cliente não encontrado."));
	}
}
