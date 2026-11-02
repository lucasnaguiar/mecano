/**
 * Contexto Autenticação (genérico): usuários da equipe, login e JWT.
 * <p>
 * Módulo do Spring Modulith: outros contextos só usam os pacotes publicados com @NamedInterface, e ModularityTests
 * falha se surgir dependência circular entre os contextos.
 */
@ApplicationModule(displayName = "Autenticação")
package br.dev.lucasaguiar.mecano.auth;

import org.springframework.modulith.ApplicationModule;
