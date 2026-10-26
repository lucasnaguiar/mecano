package br.dev.lucasaguiar.mecano.shared.domain;

import java.math.BigDecimal;

/**
 * Valor monetário em reais.
 */
public record Money(BigDecimal amount) {
}
