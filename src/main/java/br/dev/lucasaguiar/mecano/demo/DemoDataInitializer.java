package br.dev.lucasaguiar.mecano.demo;

import br.dev.lucasaguiar.mecano.auth.application.RegisterUserCommand;
import br.dev.lucasaguiar.mecano.auth.application.RegisterUserUseCase;
import br.dev.lucasaguiar.mecano.auth.domain.Role;
import br.dev.lucasaguiar.mecano.auth.domain.UserRepository;
import br.dev.lucasaguiar.mecano.catalog.application.LaborOperationData;
import br.dev.lucasaguiar.mecano.catalog.application.RegisterLaborOperationUseCase;
import br.dev.lucasaguiar.mecano.catalog.domain.LaborOperationId;
import br.dev.lucasaguiar.mecano.customer.application.CustomerData;
import br.dev.lucasaguiar.mecano.customer.application.RegisterCustomerUseCase;
import br.dev.lucasaguiar.mecano.customer.application.RegisterVehicleUseCase;
import br.dev.lucasaguiar.mecano.customer.application.VehicleData;
import br.dev.lucasaguiar.mecano.customer.domain.customer.CustomerId;
import br.dev.lucasaguiar.mecano.customer.domain.vehicle.VehicleId;
import br.dev.lucasaguiar.mecano.inventory.application.RegisterPartCommand;
import br.dev.lucasaguiar.mecano.inventory.application.RegisterPartUseCase;
import br.dev.lucasaguiar.mecano.inventory.application.RegisterStockEntryUseCase;
import br.dev.lucasaguiar.mecano.inventory.domain.PartId;
import br.dev.lucasaguiar.mecano.inventory.domain.PartType;
import br.dev.lucasaguiar.mecano.shared.domain.UnitOfMeasure;
import br.dev.lucasaguiar.mecano.shared.domain.UserId;
import br.dev.lucasaguiar.mecano.workorder.application.AddLaborUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.AddPartUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.ApproveEstimateUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.CancelWorkOrderUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.CompleteWorkOrderUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.DeliverVehicleUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.OpenWorkOrderUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.SendEstimateUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.StartDiagnosisUseCase;
import br.dev.lucasaguiar.mecano.workorder.application.StartExecutionUseCase;
import br.dev.lucasaguiar.mecano.workorder.domain.WorkOrderId;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Popula a base para demonstração (perfil "demo"): equipe, catálogo, estoque, clientes, veículos e OS em status
 * variados. Os dados passam pelos mesmos casos de uso da API, então respeitam todas as regras e geram histórico,
 * reservas e movimentações de estoque. Roda só uma vez: se a equipe de demonstração já existe, não faz nada.
 */
@Component
@Profile("demo & !test")
class DemoDataInitializer implements ApplicationRunner {

	static final String ATTENDANT_EMAIL = "atendente@mecano.local";
	static final String MECHANIC_EMAIL = "mecanico@mecano.local";
	static final String STOCK_MANAGER_EMAIL = "estoque@mecano.local";

	private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

	private final String password;
	private final UserRepository userRepository;
	private final RegisterUserUseCase registerUser;
	private final RegisterLaborOperationUseCase registerLaborOperation;
	private final RegisterPartUseCase registerPart;
	private final RegisterStockEntryUseCase registerStockEntry;
	private final RegisterCustomerUseCase registerCustomer;
	private final RegisterVehicleUseCase registerVehicle;
	private final OpenWorkOrderUseCase openWorkOrder;
	private final StartDiagnosisUseCase startDiagnosis;
	private final AddLaborUseCase addLabor;
	private final AddPartUseCase addPart;
	private final SendEstimateUseCase sendEstimate;
	private final ApproveEstimateUseCase approveEstimate;
	private final StartExecutionUseCase startExecution;
	private final CompleteWorkOrderUseCase completeWorkOrder;
	private final DeliverVehicleUseCase deliverVehicle;
	private final CancelWorkOrderUseCase cancelWorkOrder;

	DemoDataInitializer(@Value("${app.demo.password}") String password, UserRepository userRepository,
			RegisterUserUseCase registerUser, RegisterLaborOperationUseCase registerLaborOperation,
			RegisterPartUseCase registerPart, RegisterStockEntryUseCase registerStockEntry,
			RegisterCustomerUseCase registerCustomer, RegisterVehicleUseCase registerVehicle,
			OpenWorkOrderUseCase openWorkOrder, StartDiagnosisUseCase startDiagnosis, AddLaborUseCase addLabor,
			AddPartUseCase addPart, SendEstimateUseCase sendEstimate, ApproveEstimateUseCase approveEstimate,
			StartExecutionUseCase startExecution, CompleteWorkOrderUseCase completeWorkOrder,
			DeliverVehicleUseCase deliverVehicle, CancelWorkOrderUseCase cancelWorkOrder) {
		this.password = password;
		this.userRepository = userRepository;
		this.registerUser = registerUser;
		this.registerLaborOperation = registerLaborOperation;
		this.registerPart = registerPart;
		this.registerStockEntry = registerStockEntry;
		this.registerCustomer = registerCustomer;
		this.registerVehicle = registerVehicle;
		this.openWorkOrder = openWorkOrder;
		this.startDiagnosis = startDiagnosis;
		this.addLabor = addLabor;
		this.addPart = addPart;
		this.sendEstimate = sendEstimate;
		this.approveEstimate = approveEstimate;
		this.startExecution = startExecution;
		this.completeWorkOrder = completeWorkOrder;
		this.deliverVehicle = deliverVehicle;
		this.cancelWorkOrder = cancelWorkOrder;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (userRepository.existsByEmail(ATTENDANT_EMAIL)) {
			log.info("Dados de demonstração já existem; nada a fazer.");
			return;
		}

		var attendant = user("Ana Atendente", ATTENDANT_EMAIL, Role.ATTENDANT);
		var mechanic = user("Bruno Mecânico", MECHANIC_EMAIL, Role.MECHANIC);
		var stockManager = user("Carla Estoque", STOCK_MANAGER_EMAIL, Role.STOCK_MANAGER);

		var oilChange = labor("Troca de óleo e filtro", "Drenagem do óleo, troca do filtro e reabastecimento",
				"120.00", 60);
		var brakes = labor("Troca de pastilhas de freio", "Substituição das pastilhas dianteiras", "180.00", 90);
		var alignment = labor("Alinhamento e balanceamento", "Alinhamento da direção e balanceamento das rodas",
				"150.00", 60);
		var diagnosis = labor("Diagnóstico eletrônico", "Leitura de falhas com scanner", "90.00", 30);

		// Velas e pastilhas começam no estoque mínimo ou perto dele, para a consulta atOrBelowMinimum.
		var oil = part("OLEO-5W30", "Óleo de motor 5W30", PartType.SUPPLY, UnitOfMeasure.L, "45.90", "8", "40",
				stockManager);
		var oilFilter = part("FILTRO-OLEO", "Filtro de óleo", PartType.PART, UnitOfMeasure.UN, "35.00", "3", "12",
				stockManager);
		var brakePads = part("PASTILHA-DIANT", "Jogo de pastilhas dianteiras", PartType.PART, UnitOfMeasure.UN,
				"210.00", "2", "4", stockManager);
		var sparkPlug = part("VELA-IGNICAO", "Vela de ignição", PartType.PART, UnitOfMeasure.UN, "28.50", "8", "6",
				stockManager);

		var maria = customer("52998224725", "Maria Oliveira", "maria@email.com", "11987654321");
		var joao = customer("39053344705", "João Santos", "joao@email.com", "11912345678");
		var pedro = customer("16899535009", "Pedro Almeida", null, "21998887766");
		var transportadora = customer("11222333000181", "Transportadora Rápida Ltda", "frota@rapida.com.br", "1133334444");

		var onix = vehicle(maria, "BRA2E19", "Chevrolet", "Onix", 2021);
		var gol = vehicle(joao, "FJK4321", "Volkswagen", "Gol", 2015);
		var hb20 = vehicle(pedro, "RIO3A45", "Hyundai", "HB20", 2019);
		var strada = vehicle(transportadora, "QWE1J23", "Fiat", "Strada", 2023);
		var saveiro = vehicle(transportadora, "KMN5678", "Volkswagen", "Saveiro", 2018);
		var corolla = vehicle(maria, "TOY9C87", "Toyota", "Corolla", 2022);

		// Recebida: aguardando o mecânico.
		open(hb20, pedro, "Barulho ao passar em lombadas", attendant);

		// Em diagnóstico, com itens já incluídos.
		var inDiagnosis = open(saveiro, transportadora, "Luz da injeção acesa", attendant);
		startDiagnosis.execute(inDiagnosis, mechanic);
		addLabor.execute(inDiagnosis, diagnosis, 1);
		addPart.execute(inDiagnosis, sparkPlug, new BigDecimal("4"));

		// Aguardando aprovação do cliente (orçamento pendente).
		var awaiting = open(gol, joao, "Freio fazendo barulho", attendant);
		startDiagnosis.execute(awaiting, mechanic);
		addLabor.execute(awaiting, brakes, 1);
		addPart.execute(awaiting, brakePads, BigDecimal.ONE);
		sendEstimate.execute(awaiting, "Pastilhas dianteiras no limite de desgaste", attendant);

		// Em execução: orçamento aprovado e peças baixadas.
		var inProgress = open(strada, transportadora, "Revisão dos 30 mil km", attendant);
		startDiagnosis.execute(inProgress, mechanic);
		addLabor.execute(inProgress, oilChange, 1);
		addLabor.execute(inProgress, alignment, 1);
		addPart.execute(inProgress, oil, new BigDecimal("3.5"));
		addPart.execute(inProgress, oilFilter, BigDecimal.ONE);
		var inProgressCode = sendEstimate.execute(inProgress, "Revisão preventiva conforme manual", attendant)
				.getTrackingCode().value();
		approveEstimate.execute(inProgressCode);
		startExecution.execute(inProgress, mechanic);

		// Entregue: ciclo completo, entra no tempo médio de execução.
		var delivered = open(onix, maria, "Troca de óleo vencida", attendant);
		startDiagnosis.execute(delivered, mechanic);
		addLabor.execute(delivered, oilChange, 1);
		addPart.execute(delivered, oil, new BigDecimal("4"));
		addPart.execute(delivered, oilFilter, BigDecimal.ONE);
		var deliveredCode = sendEstimate.execute(delivered, null, attendant).getTrackingCode().value();
		approveEstimate.execute(deliveredCode);
		startExecution.execute(delivered, mechanic);
		completeWorkOrder.execute(delivered, mechanic);
		deliverVehicle.execute(delivered, attendant);

		// Cancelada pela equipe com o orçamento aprovado: a reserva foi liberada.
		var canceled = open(corolla, maria, "Alinhamento puxando para a direita", attendant);
		startDiagnosis.execute(canceled, mechanic);
		addLabor.execute(canceled, alignment, 1);
		var canceledCode = sendEstimate.execute(canceled, null, attendant).getTrackingCode().value();
		approveEstimate.execute(canceledCode);
		cancelWorkOrder.execute(canceled, "Cliente preferiu levar o veículo a uma concessionária", attendant);

		log.info("Dados de demonstração criados. Equipe: {}, {} e {} (senha definida em DEMO_PASSWORD).",
				ATTENDANT_EMAIL, MECHANIC_EMAIL, STOCK_MANAGER_EMAIL);
	}

	private UserId user(String name, String email, Role role) {
		return registerUser.execute(new RegisterUserCommand(name, email, password, role)).getId();
	}

	private LaborOperationId labor(String name, String description, String price, int minutes) {
		return registerLaborOperation.execute(new LaborOperationData(name, description, new BigDecimal(price), minutes))
				.getId();
	}

	private PartId part(String code, String name, PartType type, UnitOfMeasure unit, String price, String minimum,
			String stock, UserId registeredBy) {
		var id = registerPart.execute(new RegisterPartCommand(code, name, type, unit, new BigDecimal(price),
				new BigDecimal(minimum))).getId();
		registerStockEntry.execute(id, new BigDecimal(stock), registeredBy);
		return id;
	}

	private CustomerId customer(String taxId, String name, String email, String phone) {
		return registerCustomer.execute(taxId, new CustomerData(name, email, phone)).getId();
	}

	private VehicleId vehicle(CustomerId owner, String plate, String brand, String model, int year) {
		return registerVehicle.execute(owner, plate, new VehicleData(brand, model, year)).getId();
	}

	private WorkOrderId open(VehicleId vehicle, CustomerId customer, String problem, UserId openedBy) {
		return openWorkOrder.execute(customer, vehicle, problem, openedBy).getId();
	}
}
