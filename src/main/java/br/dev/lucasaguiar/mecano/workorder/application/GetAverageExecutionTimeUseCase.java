package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderRepository;
import java.time.Duration;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Calcula o tempo médio de execução (de IN_PROGRESS a COMPLETED) das OS finalizadas.
 */
@Service
public class GetAverageExecutionTimeUseCase {

	private final WorkOrderRepository workOrderRepository;

	public GetAverageExecutionTimeUseCase(WorkOrderRepository workOrderRepository) {
		this.workOrderRepository = workOrderRepository;
	}

	@Transactional(readOnly = true)
	public Optional<Duration> execute() {
		return workOrderRepository.averageExecutionTime();
	}
}
