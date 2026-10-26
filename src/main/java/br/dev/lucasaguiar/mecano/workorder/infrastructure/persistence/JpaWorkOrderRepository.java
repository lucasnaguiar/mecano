package br.dev.lucasaguiar.mecano.workorder.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderRepository;
import org.springframework.stereotype.Repository;

/**
 * Implementação JPA de WorkOrderRepository.
 */
@Repository
public class JpaWorkOrderRepository implements WorkOrderRepository {
}
