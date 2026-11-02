/**
 * Contexto Cliente (suporte): clientes e seus veículos.
 * <p>
 * Módulo do Spring Modulith: outros contextos só usam os pacotes publicados com @NamedInterface, e ModularityTests
 * falha se surgir dependência circular entre os contextos.
 */
@ApplicationModule(displayName = "Cliente")
package br.dev.lucasaguiar.mecano.customer;

import org.springframework.modulith.ApplicationModule;
