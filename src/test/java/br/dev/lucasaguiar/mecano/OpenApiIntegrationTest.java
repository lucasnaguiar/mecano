package br.dev.lucasaguiar.mecano;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class OpenApiIntegrationTest extends ApiIntegrationTest {

	@Test
	void documentsTheApiWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("Mecano API"))
				.andExpect(jsonPath("$.components.securitySchemes.bearer-jwt.scheme").value("bearer"))
				.andExpect(jsonPath("$.tags[*].name", Matchers.hasItems("Ordens de serviço", "Clientes", "Estoque",
						"Catálogo de serviços", "Acompanhamento pelo cliente")));
		mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
	}
}
