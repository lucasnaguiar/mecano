package br.dev.lucasaguiar.mecano;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Base para testes de API: sobe a aplicação com PostgreSQL do Testcontainers e o administrador inicial.
 * Os dados persistem entre os testes do mesmo contexto, então cada teste gera documentos e placas próprios.
 */
@SpringBootTest(properties = {
		"app.security.admin.email=" + ApiIntegrationTest.ADMIN_EMAIL,
		"app.security.admin.password=" + ApiIntegrationTest.ADMIN_PASSWORD
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class ApiIntegrationTest {

	protected static final String ADMIN_EMAIL = "admin@mecano.local";
	protected static final String ADMIN_PASSWORD = "admin-password";

	@Autowired
	protected MockMvc mockMvc;

	private String adminToken;

	protected String adminToken() throws Exception {
		if (adminToken == null) {
			adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
		}
		return adminToken;
	}

	protected String login(String email, String password) throws Exception {
		var response = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\": \"%s\", \"password\": \"%s\"}".formatted(email, password)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(response, "$.accessToken");
	}

	/** Cadastra um usuário da equipe com o perfil informado e devolve o token dele. */
	protected String tokenFor(String role) throws Exception {
		var email = TestData.randomCode(role).toLowerCase() + "@mecano.local";
		perform(post("/api/admin/users"), """
				{"name": "Usuário %s", "email": "%s", "password": "senha-forte", "role": "%s"}
				""".formatted(role, email, role))
				.andExpect(status().isCreated());
		return login(email, "senha-forte");
	}

	protected ResultActions perform(MockHttpServletRequestBuilder request, String json) throws Exception {
		return performAs(adminToken(), request, json);
	}

	protected ResultActions performAs(String token, MockHttpServletRequestBuilder request, String json)
			throws Exception {
		request.header("Authorization", "Bearer " + token);
		if (json != null) {
			request.contentType(MediaType.APPLICATION_JSON).content(json);
		}
		return mockMvc.perform(request);
	}

	protected ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
		return perform(request, null);
	}

	protected static <T> T read(ResultActions result, String path) throws Exception {
		return JsonPath.read(result.andReturn().getResponse().getContentAsString(), path);
	}
}
