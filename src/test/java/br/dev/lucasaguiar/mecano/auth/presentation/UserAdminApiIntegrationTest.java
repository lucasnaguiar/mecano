package br.dev.lucasaguiar.mecano.auth.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.dev.lucasaguiar.mecano.ApiIntegrationTest;
import br.dev.lucasaguiar.mecano.TestData;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class UserAdminApiIntegrationTest extends ApiIntegrationTest {

	@Test
	void adminManagesTeamUsers() throws Exception {
		var email = TestData.randomCode("joao").toLowerCase() + "@mecano.local";
		String id = read(perform(post("/api/admin/users"), """
				{"name": "João", "email": "%s", "password": "senha-forte", "role": "ATTENDANT"}
				""".formatted(email)).andExpect(status().isCreated()).andExpect(jsonPath("$.active").value(true)), "$.id");

		perform(get("/api/admin/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].email", Matchers.hasItems(email, ADMIN_EMAIL)));

		perform(put("/api/admin/users/" + id + "/role"), """
				{"role": "MECHANIC"}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("MECHANIC"));

		perform(post("/api/admin/users/" + id + "/deactivation"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.active").value(false));
		mockMvc.perform(post("/api/auth/login").contentType("application/json")
						.content("{\"email\": \"%s\", \"password\": \"senha-forte\"}".formatted(email)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."));

		perform(post("/api/admin/users/" + id + "/activation"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.active").value(true));
		login(email, "senha-forte");
	}

	@Test
	void adminCannotDeactivateOrDemoteSelf() throws Exception {
		List<String> adminIds = read(perform(get("/api/admin/users")), "$[?(@.email == '" + ADMIN_EMAIL + "')].id");
		var adminId = adminIds.getFirst();

		perform(post("/api/admin/users/" + adminId + "/deactivation"))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.detail").value("Você não pode alterar o perfil nem desativar o próprio usuário."));
		perform(put("/api/admin/users/" + adminId + "/role"), """
				{"role": "MECHANIC"}
				""")
				.andExpect(status().isUnprocessableContent());
	}

	@Test
	void onlyAdminManagesUsers() throws Exception {
		var attendant = tokenFor("ATTENDANT");

		performAs(attendant, get("/api/admin/users"), null).andExpect(status().isForbidden());
		perform(post("/api/admin/users/00000000-0000-0000-0000-000000000000/activation"))
				.andExpect(status().isNotFound());
	}
}
