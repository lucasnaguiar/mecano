package br.dev.lucasaguiar.mecano.shared.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.stereotype.Component;

/**
 * Documenta as respostas de erro (formato ProblemDetail) em todas as operações, com exemplos em português, sem
 * repetir anotações em cada controller. As regras de 403 acompanham as de SecurityConfig.
 */
@Component
class ErrorResponsesCustomizer implements GlobalOpenApiCustomizer {

	static final String PROBLEM_SCHEMA = "Problema";

	private static final Set<PathItem.HttpMethod> WRITES = Set.of(PathItem.HttpMethod.POST, PathItem.HttpMethod.PUT,
			PathItem.HttpMethod.PATCH, PathItem.HttpMethod.DELETE);

	@Override
	public void customise(OpenAPI openApi) {
		openApi.getComponents().addSchemas(PROBLEM_SCHEMA, problemSchema());
		if (openApi.getPaths() == null) {
			return;
		}
		openApi.getPaths().forEach((path, item) -> item.readOperationsMap()
				.forEach((method, operation) -> document(path, method, operation)));
	}

	private static void document(String path, PathItem.HttpMethod method, Operation operation) {
		var admin = path.startsWith("/api/admin/");
		var write = WRITES.contains(method);
		if (operation.getRequestBody() != null || operation.getParameters() != null) {
			add(operation, 400, "Dados inválidos", Map.of(
					"valor inválido", problem(400, "CPF ou CNPJ inválido.", path),
					"campos obrigatórios", validationProblem(path)));
		}
		if (admin) {
			add(operation, 401, "Token ausente, inválido ou expirado", Map.of("sem token", problem(401,
					"Autenticação necessária: informe um token válido no cabeçalho Authorization (Bearer).", path)));
		}
		if (admin && requiresSpecificRole(path, method)) {
			add(operation, 403, "O perfil do usuário não tem permissão", Map.of("perfil sem permissão",
					problem(403, "Seu perfil não tem permissão para esta operação.", path)));
		}
		if (path.contains("{")) {
			add(operation, 404, "Recurso não encontrado", Map.of("não encontrado",
					problem(404, "Ordem de serviço não encontrada.", path)));
		}
		if (write) {
			add(operation, 409, "Conflito: registro duplicado, com vínculos ou alterado por outra operação", Map.of(
					"duplicado", problem(409, "Já existe um cliente com o documento 12345678909.", path),
					"alteração concorrente", problem(409, "O registro foi alterado por outra operação. Tente novamente.",
							path)));
			add(operation, 422, "Regra de negócio violada", Map.of(
					"status inválido", problem(422, "Não é possível incluir serviços ou peças: a ordem de serviço "
							+ "está \"Recebida\" e precisa estar \"Em diagnóstico\".", path),
					"estoque insuficiente", problem(422, "Estoque insuficiente de \"Filtro de óleo\": disponível 5 UN, "
							+ "necessário 6 UN.", path)));
		}
	}

	/** Espelha as regras por perfil de SecurityConfig; as demais rotas administrativas aceitam qualquer perfil. */
	private static boolean requiresSpecificRole(String path, PathItem.HttpMethod method) {
		if (path.startsWith("/api/admin/users") || path.startsWith("/api/admin/reports")) {
			return true;
		}
		var catalogOrStock = path.startsWith("/api/admin/labor-operations") || path.startsWith("/api/admin/parts");
		return catalogOrStock && method != PathItem.HttpMethod.GET;
	}

	private static void add(Operation operation, int status, String description, Map<String, Object> examples) {
		if (operation.getResponses() == null) {
			operation.setResponses(new ApiResponses());
		}
		var code = String.valueOf(status);
		if (operation.getResponses().containsKey(code)) {
			return;
		}
		var media = new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + PROBLEM_SCHEMA));
		examples.forEach((name, value) -> media.addExamples(name, new Example().value(value)));
		operation.getResponses().addApiResponse(code, new ApiResponse().description(description)
				.content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE,
						media)));
	}

	private static Map<String, Object> problem(int status, String detail, String path) {
		var problem = new LinkedHashMap<String, Object>();
		problem.put("type", "about:blank");
		problem.put("title", org.springframework.http.HttpStatus.valueOf(status).getReasonPhrase());
		problem.put("status", status);
		problem.put("detail", detail);
		problem.put("instance", path);
		return problem;
	}

	private static Map<String, Object> validationProblem(String path) {
		var problem = problem(400, "Dados inválidos.", path);
		problem.put("errors", Map.of("name", "O nome é obrigatório."));
		return problem;
	}

	private static Schema<?> problemSchema() {
		return new ObjectSchema()
				.description("Erro no formato ProblemDetail (RFC 9457)")
				.addProperty("type", new StringSchema().example("about:blank"))
				.addProperty("title", new StringSchema().description("Descrição padrão do status HTTP"))
				.addProperty("status", new IntegerSchema().description("Código HTTP"))
				.addProperty("detail", new StringSchema().description("Mensagem em português para o usuário"))
				.addProperty("instance", new StringSchema().description("Caminho da requisição"))
				.addProperty("errors", new MapSchema().additionalProperties(new StringSchema())
						.description("Erros por campo, presentes só na validação do corpo da requisição"));
	}
}
