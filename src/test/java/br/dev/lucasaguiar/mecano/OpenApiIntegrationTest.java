package br.dev.lucasaguiar.mecano;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class OpenApiIntegrationTest extends ApiIntegrationTest {

	private static final String PROBLEM = "content['application/problem+json']";

	@Test
	void documentsTheApiWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/v3/api-docs/equipe"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("Mecano API"))
				.andExpect(jsonPath("$.components.securitySchemes.bearer-jwt.scheme").value("bearer"))
				.andExpect(jsonPath("$.tags[*].name", Matchers.hasItems("Ordens de serviço", "Clientes", "Estoque",
						"Catálogo de serviços")));
		mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
	}

	@Test
	void separatesStaffAndCustomerGroups() throws Exception {
		mockMvc.perform(get("/v3/api-docs/swagger-config"))
				.andExpect(jsonPath("$.urls[*].name", Matchers.containsInAnyOrder("Equipe", "Cliente")));
		mockMvc.perform(get("/v3/api-docs/equipe"))
				.andExpect(jsonPath("$.paths['/api/auth/login']").exists())
				.andExpect(jsonPath("$.paths['/api/customer/work-orders/{trackingCode}']").doesNotExist());
		mockMvc.perform(get("/v3/api-docs/cliente"))
				.andExpect(jsonPath("$.paths['/api/customer/work-orders/{trackingCode}']").exists())
				.andExpect(jsonPath("$.paths['/api/admin/work-orders']").doesNotExist());
	}

	@Test
	void documentsErrorResponsesWithExamples() throws Exception {
		var cancel = "$.paths['/api/admin/work-orders/{id}/cancellation'].post.responses";
		mockMvc.perform(get("/v3/api-docs/equipe"))
				.andExpect(jsonPath("$.components.schemas.Problema.properties.detail").exists())
				.andExpect(jsonPath(cancel + "['400']." + PROBLEM + ".examples").exists())
				.andExpect(jsonPath(cancel + "['401']." + PROBLEM + ".examples['sem token'].value.status").value(401))
				.andExpect(jsonPath(cancel + "['404']").exists())
				.andExpect(jsonPath(cancel + "['409']").exists())
				.andExpect(jsonPath(cancel + "['422']." + PROBLEM + ".examples['status inválido'].value.detail")
						.isString())
				.andExpect(jsonPath(cancel + "['403']").doesNotExist())
				.andExpect(jsonPath("$.paths['/api/admin/users'].get.responses['403']").exists())
				.andExpect(jsonPath("$.paths['/api/admin/parts'].post.responses['403']").exists())
				.andExpect(jsonPath("$.paths['/api/admin/parts'].get.responses['403']").doesNotExist())
				.andExpect(jsonPath("$.paths['/api/auth/login'].post.responses['401'].description")
						.value("E-mail ou senha inválidos"))
				.andExpect(jsonPath("$.paths['/api/auth/login'].post.responses['429']").exists())
				.andExpect(jsonPath(cancel + "['429']").doesNotExist());
		mockMvc.perform(get("/v3/api-docs/cliente"))
				.andExpect(jsonPath("$.paths['/api/customer/work-orders/{trackingCode}'].get.responses['401']")
						.doesNotExist())
				.andExpect(jsonPath("$.paths['/api/customer/work-orders/{trackingCode}'].get.responses['404']")
						.exists())
				.andExpect(jsonPath("$.paths['/api/customer/work-orders/{trackingCode}'].get.responses['429']")
						.exists());
	}
}
