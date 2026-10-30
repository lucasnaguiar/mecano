package br.dev.lucasaguiar.mecano.catalog.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.dev.lucasaguiar.mecano.ApiIntegrationTest;
import br.dev.lucasaguiar.mecano.TestData;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class LaborOperationApiIntegrationTest extends ApiIntegrationTest {

	@Test
	void adminManagesCatalog() throws Exception {
		var name = TestData.randomCode("Revisao");
		var created = perform(post("/api/admin/labor-operations"), """
				{"name": "%s", "description": "Revisão completa", "price": 350, "estimatedDurationMinutes": 180}
				""".formatted(name))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.price").value(350.00))
				.andExpect(jsonPath("$.active").value(true));
		String id = read(created, "$.id");

		perform(post("/api/admin/labor-operations"), """
				{"name": "%s", "price": 10, "estimatedDurationMinutes": 10}
				""".formatted(name))
				.andExpect(status().isConflict());

		perform(put("/api/admin/labor-operations/" + id), """
				{"name": "%s", "price": 399.90, "estimatedDurationMinutes": 200}
				""".formatted(name))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.price").value(399.90));

		perform(delete("/api/admin/labor-operations/" + id)).andExpect(status().isNoContent());
		perform(get("/api/admin/labor-operations"))
				.andExpect(jsonPath("$[*].id", Matchers.not(Matchers.hasItem(id))));
		perform(get("/api/admin/labor-operations").param("includeInactive", "true"))
				.andExpect(jsonPath("$[*].id", Matchers.hasItem(id)));
	}

	@Test
	void onlyAdminChangesCatalog() throws Exception {
		var mechanic = tokenFor("MECHANIC");

		performAs(mechanic, get("/api/admin/labor-operations"), null).andExpect(status().isOk());
		performAs(mechanic, post("/api/admin/labor-operations"), """
				{"name": "Qualquer", "price": 10, "estimatedDurationMinutes": 10}
				""")
				.andExpect(status().isForbidden());
	}

	@Test
	void validatesInput() throws Exception {
		perform(post("/api/admin/labor-operations"), """
				{"name": "Sem preço", "estimatedDurationMinutes": 0}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.price").value("O preço é obrigatório."))
				.andExpect(jsonPath("$.errors.estimatedDurationMinutes").exists());
	}
}
