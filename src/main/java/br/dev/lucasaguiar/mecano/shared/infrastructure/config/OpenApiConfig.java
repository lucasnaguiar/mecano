package br.dev.lucasaguiar.mecano.shared.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Swagger (springdoc-openapi).
 */
@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Mecano API")
						.description("API de gestão de oficina mecânica: ordens de serviço, orçamentos, clientes, veículos, serviços e estoque.")
						.version("v1"))
				.components(new Components()
						.addSecuritySchemes("bearer-jwt", new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("Token obtido em POST /api/auth/login")));
	}

	/** APIs da equipe da oficina: login e rotas administrativas (JWT). */
	@Bean
	GroupedOpenApi staffApi() {
		return GroupedOpenApi.builder()
				.group("equipe")
				.displayName("Equipe")
				.pathsToMatch("/api/auth/**", "/api/admin/**")
				.build();
	}

	/** API pública do cliente: acompanhamento e resposta ao orçamento pelo código de acompanhamento. */
	@Bean
	GroupedOpenApi customerApi() {
		return GroupedOpenApi.builder()
				.group("cliente")
				.displayName("Cliente")
				.pathsToMatch("/api/customer/**")
				.build();
	}
}
