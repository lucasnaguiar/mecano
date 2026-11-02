/**
 * Contexto Estoque (suporte): peças e insumos, reservas, baixas e entradas.
 * <p>
 * Módulo do Spring Modulith: outros contextos só usam os pacotes publicados com @NamedInterface, e ModularityTests
 * falha se surgir dependência circular entre os contextos.
 */
@ApplicationModule(displayName = "Estoque")
package br.dev.lucasaguiar.mecano.inventory;

import org.springframework.modulith.ApplicationModule;
