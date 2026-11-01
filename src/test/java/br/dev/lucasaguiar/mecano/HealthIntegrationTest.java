package br.dev.lucasaguiar.mecano;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class HealthIntegrationTest extends ApiIntegrationTest {

	@Test
	void exposesHealthWithoutAuthenticationAndWithoutDetails() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components").doesNotExist());
		mockMvc.perform(get("/actuator/health/readiness"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
		mockMvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
	}
}
