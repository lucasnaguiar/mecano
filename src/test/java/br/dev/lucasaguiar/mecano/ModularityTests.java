package br.dev.lucasaguiar.mecano;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Verifica a estrutura de módulos (contextos delimitados) com o Spring Modulith: o build falha se surgir dependência
 * circular entre contextos. Também gera os diagramas dos contextos em target/spring-modulith-docs.
 */
class ModularityTests {

	private static final ApplicationModules MODULES = ApplicationModules.of(MecanoApplication.class);

	@Test
	void verifiesModuleStructure() {
		MODULES.verify();
	}

	@Test
	void writesModuleDocumentation() {
		new Documenter(MODULES)
				.writeModulesAsPlantUml()
				.writeIndividualModulesAsPlantUml()
				.writeModuleCanvases();
	}
}
