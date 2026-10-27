package br.dev.lucasaguiar.mecano.shared.domain;

import java.math.BigDecimal;

/**
 * Quantidade de itens: inteira para peças, fracionada para insumos (litros, quilos etc.).
 */
public record Quantity(BigDecimal value) {
}
