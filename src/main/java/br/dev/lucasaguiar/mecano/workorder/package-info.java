/**
 * Contexto Ordem de Serviço (subdomínio principal): ciclo de vida da OS, orçamento, execução e entrega.
 * <p>
 * Módulo do Spring Modulith: outros contextos só usam os pacotes publicados com @NamedInterface, e ModularityTests
 * falha se surgir dependência circular entre os contextos.
 */
@ApplicationModule(displayName = "Ordem de Serviço")
package br.dev.lucasaguiar.mecano.workorder;

import org.springframework.modulith.ApplicationModule;
