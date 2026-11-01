package br.dev.lucasaguiar.mecano.workorder.application;

import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerRepository;
import br.dev.lucasaguiar.mecano.shared.application.PersonalDataMasker;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderRepository;
import br.dev.lucasaguiar.mecano.workorder.domain.event.EstimateSent;
import br.dev.lucasaguiar.mecano.workorder.domain.event.WorkOrderCompleted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Políticas "Notificar orçamento" e "Avisar veículo pronto". No MVP a notificação é registrada em log; o envio de
 * e-mail entra no lugar do log. Nome, e-mail e código de acompanhamento saem mascarados (LGPD): o código completo
 * dá acesso à OS e a equipe o consulta na própria OS.
 */
@Component
class CustomerNotificationPolicy {

	private static final Logger log = LoggerFactory.getLogger(CustomerNotificationPolicy.class);

	private final WorkOrderRepository workOrderRepository;
	private final CustomerRepository customerRepository;

	CustomerNotificationPolicy(WorkOrderRepository workOrderRepository, CustomerRepository customerRepository) {
		this.workOrderRepository = workOrderRepository;
		this.customerRepository = customerRepository;
	}

	@EventListener
	void on(EstimateSent event) {
		notifyCustomer(event.workOrderId(), "o orçamento da OS %d está disponível para aprovação");
	}

	@EventListener
	void on(WorkOrderCompleted event) {
		notifyCustomer(event.workOrderId(), "o veículo da OS %d está pronto para retirada");
	}

	private void notifyCustomer(WorkOrderId workOrderId, String message) {
		workOrderRepository.findById(workOrderId).ifPresent(workOrder -> customerRepository
				.findById(workOrder.getCustomerId())
				.ifPresent(customer -> log.info("Notificação para {} <{}>: {} (código de acompanhamento {}).",
						PersonalDataMasker.name(customer.getName()),
						customer.getEmail() == null ? "sem e-mail" : PersonalDataMasker.email(customer.getEmail()),
						message.formatted(workOrder.getNumber()),
						PersonalDataMasker.lastDigits(workOrder.getTrackingCode().value(), 4))));
	}
}
