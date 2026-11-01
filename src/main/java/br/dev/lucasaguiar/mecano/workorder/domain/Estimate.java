package br.dev.lucasaguiar.mecano.workorder.domain;

import br.dev.lucasaguiar.mecano.shared.domain.BusinessRuleException;
import br.dev.lucasaguiar.mecano.shared.domain.Money;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Orçamento enviado ao cliente, com o total congelado no envio. Pode ser aprovado ou rejeitado uma única vez,
 * ou encerrado sem resposta se a OS for cancelada.
 */
public class Estimate {

	private final UUID id;
	private final Money totalAmount;
	private final Instant sentAt;
	private EstimateStatus status;
	private Instant respondedAt;

	private Estimate(UUID id, EstimateStatus status, Money totalAmount, Instant sentAt, Instant respondedAt) {
		this.id = Objects.requireNonNull(id);
		this.status = Objects.requireNonNull(status);
		this.totalAmount = Objects.requireNonNull(totalAmount);
		this.sentAt = Objects.requireNonNull(sentAt);
		this.respondedAt = respondedAt;
	}

	static Estimate send(Money totalAmount, Instant now) {
		return new Estimate(UUID.randomUUID(), EstimateStatus.PENDING, totalAmount, now, null);
	}

	public static Estimate restore(UUID id, EstimateStatus status, Money totalAmount, Instant sentAt,
			Instant respondedAt) {
		return new Estimate(id, status, totalAmount, sentAt, respondedAt);
	}

	void approve(Instant now) {
		respond(EstimateStatus.APPROVED, now);
	}

	void reject(Instant now) {
		respond(EstimateStatus.REJECTED, now);
	}

	void cancel(Instant now) {
		respond(EstimateStatus.CANCELED, now);
	}

	private void respond(EstimateStatus response, Instant now) {
		if (!isPending()) {
			throw new BusinessRuleException("Este orçamento já foi respondido.");
		}
		status = response;
		respondedAt = now;
	}

	public boolean isPending() {
		return status == EstimateStatus.PENDING;
	}

	public boolean isApproved() {
		return status == EstimateStatus.APPROVED;
	}

	public UUID getId() {
		return id;
	}

	public EstimateStatus getStatus() {
		return status;
	}

	public Money getTotalAmount() {
		return totalAmount;
	}

	public Instant getSentAt() {
		return sentAt;
	}

	public Instant getRespondedAt() {
		return respondedAt;
	}
}
