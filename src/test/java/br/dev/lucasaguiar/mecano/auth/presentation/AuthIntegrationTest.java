package br.dev.lucasaguiar.mecano.auth.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.dev.lucasaguiar.mecano.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
		"app.security.admin.email=admin@mecano.local",
		"app.security.admin.password=admin-password"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuthIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void initialAdminLogsInAndRegistersUser() throws Exception {
		var adminToken = login("admin@mecano.local", "admin-password");

		mockMvc.perform(post("/api/admin/users")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name": "Carlos", "email": "carlos@mecano.local",
								 "password": "carlos-password", "role": "MECHANIC"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("carlos@mecano.local"))
				.andExpect(jsonPath("$.role").value("MECHANIC"));

		var mechanicToken = login("carlos@mecano.local", "carlos-password");

		mockMvc.perform(post("/api/admin/users")
						.header("Authorization", "Bearer " + mechanicToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name": "Outro", "email": "outro@mecano.local",
								 "password": "outro-password", "role": "ADMIN"}
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	void rejectsInvalidCredentials() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "admin@mecano.local", "password": "senha-errada"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."));
	}

	@Test
	void adminApisRequireToken() throws Exception {
		mockMvc.perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isUnauthorized());
	}

	private String login(String email, String password) throws Exception {
		var response = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\": \"%s\", \"password\": \"%s\"}".formatted(email, password)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(response, "$.accessToken");
	}
}
