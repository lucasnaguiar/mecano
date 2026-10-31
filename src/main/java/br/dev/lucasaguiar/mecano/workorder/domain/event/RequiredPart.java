package br.dev.lucasaguiar.mecano.workorder.domain.event;

import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.shared.domain.Quantity;

/**
 * Quantidade total de uma peça ou insumo exigida pela OS (somada entre itens repetidos).
 */
public record RequiredPart(PartId partId, Quantity quantity) {
}
