package br.dev.lucasaguiar.mecano.catalog.application;

import java.math.BigDecimal;

/**
 * Dados de um serviço do catálogo.
 */
public record LaborOperationData(String name, String description, BigDecimal price, int estimatedDurationMinutes) {
}
