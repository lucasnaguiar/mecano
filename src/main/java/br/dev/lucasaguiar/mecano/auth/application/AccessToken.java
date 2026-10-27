package br.dev.lucasaguiar.mecano.auth.application;

import java.time.Duration;

/**
 * Token de acesso emitido no login.
 */
public record AccessToken(String value, Duration expiresIn) {
}
