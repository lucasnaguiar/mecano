package br.dev.lucasaguiar.mecano.shared.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.dev.lucasaguiar.mecano.TestcontainersConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest(properties = {
		"app.rate-limit.login.max-requests=3",
		"app.rate-limit.tracking.max-requests=2"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class RateLimitIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void limitsLoginAttemptsPerIp() throws Exception {
		for (var i = 0; i < 3; i++) {
			mockMvc.perform(login("10.1.0.1")).andExpect(status().isUnauthorized());
		}

		mockMvc.perform(login("10.1.0.1"))
				.andExpect(status().isTooManyRequests())
				.andExpect(header().string("Retry-After", Matchers.matchesPattern("\\d+")))
				.andExpect(jsonPath("$.detail", Matchers.startsWith("Muitas tentativas.")));
		mockMvc.perform(login("10.1.0.2")).andExpect(status().isUnauthorized());
	}

	@Test
	void limitsTrackingCodeLookupsPerIp() throws Exception {
		mockMvc.perform(tracking("10.2.0.1")).andExpect(status().isNotFound());
		mockMvc.perform(tracking("10.2.0.1")).andExpect(status().isNotFound());

		mockMvc.perform(tracking("10.2.0.1")).andExpect(status().isTooManyRequests());
		mockMvc.perform(get("/api/admin/work-orders").with(ip("10.2.0.1"))).andExpect(status().isUnauthorized());
	}

	private static MockHttpServletRequestBuilder login(String ip) {
		return post("/api/auth/login").with(ip(ip)).contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\": \"ninguem@mecano.local\", \"password\": \"senha-errada\"}");
	}

	private static MockHttpServletRequestBuilder tracking(String ip) {
		return get("/api/customer/work-orders/ABCDEFGHJKLM").with(ip(ip));
	}

	private static org.springframework.test.web.servlet.request.RequestPostProcessor ip(String address) {
		return request -> {
			request.setRemoteAddr(address);
			return request;
		};
	}
}
