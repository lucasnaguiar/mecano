/**
 * Objetos de valor, exceções e infraestrutura comuns a todos os contextos.
 * <p>
 * Módulo do Spring Modulith: outros contextos só usam os pacotes publicados com @NamedInterface, e ModularityTests
 * falha se surgir dependência circular entre os contextos.
 */
@ApplicationModule(displayName = "Compartilhado")
package br.dev.lucasaguiar.mecano.shared;

import org.springframework.modulith.ApplicationModule;
