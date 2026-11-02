package br.dev.lucasaguiar.mecano.shared.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.LinkedHashMap;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

/**
 * Proteção contra força bruta nos endpoints públicos: limita, por IP, as tentativas de login e as consultas pelo
 * código de acompanhamento. Excedido o limite, responde 429 com Retry-After. O IP é o da conexão (getRemoteAddr);
 * atrás de proxy, configure server.forward-headers-strategy para usar o IP real do cliente.
 */
@Component
@EnableConfigurationProperties(RateLimitProperties.class)
class RateLimitFilter extends OncePerRequestFilter {

	private static final String LOGIN_PATH = "/api/auth/login";
	private static final String TRACKING_PREFIX = "/api/customer/";

	private final FixedWindowRateLimiter loginLimiter;
	private final FixedWindowRateLimiter trackingLimiter;
	private final JsonMapper jsonMapper;

	RateLimitFilter(RateLimitProperties properties, JsonMapper jsonMapper) {
		this.loginLimiter = new FixedWindowRateLimiter(properties.login(), Clock.systemUTC());
		this.trackingLimiter = new FixedWindowRateLimiter(properties.tracking(), Clock.systemUTC());
		this.jsonMapper = jsonMapper;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		var limiter = limiterFor(request);
		if (limiter != null) {
			var retryAfter = limiter.tryAcquire(request.getRemoteAddr());
			if (retryAfter.isPositive()) {
				reject(request, response, Math.max(1, retryAfter.toSeconds()));
				return;
			}
		}
		chain.doFilter(request, response);
	}

	private FixedWindowRateLimiter limiterFor(HttpServletRequest request) {
		var path = request.getRequestURI();
		if (LOGIN_PATH.equals(path) && "POST".equals(request.getMethod())) {
			return loginLimiter;
		}
		return path.startsWith(TRACKING_PREFIX) ? trackingLimiter : null;
	}

	private void reject(HttpServletRequest request, HttpServletResponse response, long retryAfterSeconds)
			throws IOException {
		var status = HttpStatus.TOO_MANY_REQUESTS;
		var problem = new LinkedHashMap<String, Object>();
		problem.put("type", "about:blank");
		problem.put("title", status.getReasonPhrase());
		problem.put("status", status.value());
		problem.put("detail", "Muitas tentativas. Aguarde " + retryAfterSeconds + " segundos e tente novamente.");
		problem.put("instance", request.getRequestURI());
		response.setStatus(status.value());
		response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		jsonMapper.writeValue(response.getOutputStream(), problem);
	}
}
