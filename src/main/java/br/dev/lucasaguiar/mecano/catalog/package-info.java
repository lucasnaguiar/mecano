/**
 * Contexto Catálogo (suporte): serviços oferecidos pela oficina e seus preços.
 * <p>
 * Módulo do Spring Modulith: outros contextos só usam os pacotes publicados com @NamedInterface, e ModularityTests
 * falha se surgir dependência circular entre os contextos.
 */
@ApplicationModule(displayName = "Catálogo")
package br.dev.lucasaguiar.mecano.catalog;

import org.springframework.modulith.ApplicationModule;
