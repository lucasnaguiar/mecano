package br.dev.lucasaguiar.mecano.workorder.infrastructure.persistence;

import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;

/**
 * Mapeamento JPA da tabela work_order. Itens, orçamentos e transições pertencem ao agregado e são gravados em
 * cascata; cliente e veículo são referenciados apenas pelo ID.
 */
@Entity
@Table(name = "work_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
class WorkOrderEntity {

	@Id
	private UUID id;

	/** Gerado pela sequence work_order_number_seq na inserção. */
	@Generated
	@Column(nullable = false, unique = true, insertable = false, updatable = false)
	private Long number;

	@Column(name = "tracking_code", nullable = false, unique = true, length = 32)
	private String trackingCode;

	@Column(name = "customer_id", nullable = false)
	private UUID customerId;

	@Column(name = "vehicle_id", nullable = false)
	private UUID vehicleId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private WorkOrderStatus status;

	@Column(name = "problem_description", nullable = false)
	private String problemDescription;

	@Column(name = "diagnosis_notes")
	private String diagnosisNotes;

	@Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount;

	@Version
	private Long version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "work_order_id", nullable = false, updatable = false)
	private List<LaborItemEntity> laborItems = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "work_order_id", nullable = false, updatable = false)
	private List<PartItemEntity> partItems = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "work_order_id", nullable = false, updatable = false)
	@OrderBy("sentAt")
	private List<EstimateEntity> estimates = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "work_order_id", nullable = false, updatable = false)
	@OrderBy("occurredAt")
	private List<StatusTransitionEntity> statusTransitions = new ArrayList<>();
}
