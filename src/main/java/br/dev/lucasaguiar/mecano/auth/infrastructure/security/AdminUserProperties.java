package br.dev.lucasaguiar.mecano.auth.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credenciais do administrador inicial (app.security.admin.*). Vazio = não cria.
 */
@ConfigurationProperties("app.security.admin")
public record AdminUserProperties(String name, String email, String password) {
}
