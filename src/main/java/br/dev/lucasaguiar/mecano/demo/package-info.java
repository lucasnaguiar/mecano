/**
 * Dados de demonstração (perfil demo), criados pelos casos de uso dos demais contextos.
 * <p>
 * Módulo do Spring Modulith: outros contextos só usam os pacotes publicados com @NamedInterface, e ModularityTests
 * falha se surgir dependência circular entre os contextos.
 */
@ApplicationModule(displayName = "Demonstração")
package br.dev.lucasaguiar.mecano.demo;

import org.springframework.modulith.ApplicationModule;
