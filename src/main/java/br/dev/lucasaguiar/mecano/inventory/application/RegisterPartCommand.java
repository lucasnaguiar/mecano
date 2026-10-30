package br.dev.lucasaguiar.mecano.inventory.application;

import br.dev.lucasaguiar.mecano.inventory.domain.PartType;
import br.dev.lucasaguiar.mecano.shared.domain.UnitOfMeasure;
import java.math.BigDecimal;

/**
 * Dados para cadastrar uma peça ou insumo.
 */
public record RegisterPartCommand(String code, String name, PartType type, UnitOfMeasure unitOfMeasure,
		BigDecimal unitPrice, BigDecimal minimumStock) {
}
