package br.dev.lucasaguiar.mecano.workorder.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.dev.lucasaguiar.mecano.ApiIntegrationTest;
import br.dev.lucasaguiar.mecano.TestData;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkOrderFlowIntegrationTest extends ApiIntegrationTest {

	private String customerId;
	private String vehicleId;
	private String laborOperationId;
	private String filterId;
	private String oilId;

	@BeforeEach
	void registerCustomerCatalogAndStock() throws Exception {
		customerId = read(perform(post("/api/admin/customers"), """
				{"name": "Carlos Lima", "taxId": "%s", "email": "carlos@email.com"}
				""".formatted(TestData.randomCpf())).andExpect(status().isCreated()), "$.id");
		vehicleId = read(perform(post("/api/admin/vehicles"), """
				{"customerId": "%s", "licensePlate": "%s", "brand": "VW", "model": "Gol", "modelYear": 2018}
				""".formatted(customerId, TestData.randomPlate())).andExpect(status().isCreated()), "$.id");
		laborOperationId = read(perform(post("/api/admin/labor-operations"), """
				{"name": "%s", "price": 120, "estimatedDurationMinutes": 60}
				""".formatted(TestData.randomCode("Troca de oleo"))).andExpect(status().isCreated()), "$.id");
		filterId = registerPart("FILTRO", "Filtro de óleo", "PART", "UN", "35.00", "5");
		oilId = registerPart("OLEO", "Óleo 5W30", "SUPPLY", "L", "45.90", "10");
	}

	@Test
	void attendsVehicleFromReceptionToDelivery() throws Exception {
		var opened = perform(post("/api/admin/work-orders"), """
				{"customerId": "%s", "vehicleId": "%s", "problemDescription": "Luz do óleo acesa"}
				""".formatted(customerId, vehicleId))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("RECEIVED"))
				.andExpect(jsonPath("$.number").value(Matchers.greaterThanOrEqualTo(1000)));
		String id = read(opened, "$.id");
		String trackingCode = read(opened, "$.trackingCode");
		var workOrder = "/api/admin/work-orders/" + id;

		perform(post("/api/admin/work-orders"), """
				{"customerId": "%s", "vehicleId": "%s", "problemDescription": "Outra"}
				""".formatted(customerId, vehicleId))
				.andExpect(status().isConflict());
		perform(post(workOrder + "/labor-items"), laborJson())
				.andExpect(status().isUnprocessableContent());

		perform(post(workOrder + "/diagnosis")).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("IN_DIAGNOSIS"));
		String wrongItem = read(perform(post(workOrder + "/labor-items"), laborJson())
				.andExpect(status().isCreated()), "$.laborItems[0].id");
		perform(delete(workOrder + "/labor-items/" + wrongItem)).andExpect(jsonPath("$.totalAmount").value(0));
		perform(post(workOrder + "/labor-items"), laborJson()).andExpect(status().isCreated());
		perform(post(workOrder + "/part-items"), partJson(filterId, "1")).andExpect(status().isCreated());
		perform(post(workOrder + "/part-items"), partJson(oilId, "3.5"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.totalAmount").value(315.65));

		perform(post(workOrder + "/estimate"), """
				{"diagnosisNotes": "Óleo e filtro vencidos"}
				""")
				.andExpect(jsonPath("$.status").value("AWAITING_APPROVAL"));

		var customerView = "/api/customer/work-orders/" + trackingCode.toLowerCase();
		mockMvc.perform(get(customerView))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.statusLabel").value("Aguardando aprovação"))
				.andExpect(jsonPath("$.items.length()").value(3))
				.andExpect(jsonPath("$.estimate.totalAmount").value(315.65))
				.andExpect(jsonPath("$.customerId").doesNotExist());

		mockMvc.perform(post(customerView + "/rejection"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("IN_DIAGNOSIS"))
				.andExpect(jsonPath("$.estimate.status").value("REJECTED"));
		perform(post(workOrder + "/estimate")).andExpect(status().isOk())
				.andExpect(jsonPath("$.diagnosisNotes").value("Óleo e filtro vencidos"));
		mockMvc.perform(post(customerView + "/approval"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estimate.status").value("APPROVED"));
		mockMvc.perform(post(customerView + "/approval")).andExpect(status().isUnprocessableContent());

		perform(get("/api/admin/parts/" + filterId))
				.andExpect(jsonPath("$.availableQuantity").value(4))
				.andExpect(jsonPath("$.reservedQuantity").value(1));
		perform(get("/api/admin/parts/" + oilId))
				.andExpect(jsonPath("$.availableQuantity").value(6.5))
				.andExpect(jsonPath("$.reservedQuantity").value(3.5));

		perform(post(workOrder + "/execution")).andExpect(jsonPath("$.status").value("IN_PROGRESS"));
		perform(get("/api/admin/parts/" + oilId))
				.andExpect(jsonPath("$.availableQuantity").value(6.5))
				.andExpect(jsonPath("$.reservedQuantity").value(0));
		perform(get("/api/admin/parts/" + oilId + "/movements"))
				.andExpect(jsonPath("$[0].type").value("WITHDRAWAL"))
				.andExpect(jsonPath("$[1].type").value("RESERVATION"))
				.andExpect(jsonPath("$[0].workOrderId").value(id));

		perform(post(workOrder + "/completion")).andExpect(jsonPath("$.status").value("COMPLETED"));
		perform(post(workOrder + "/delivery"))
				.andExpect(jsonPath("$.status").value("DELIVERED"))
				.andExpect(jsonPath("$.statusHistory.length()").value(8))
				.andExpect(jsonPath("$.estimates.length()").value(2));

		perform(get("/api/admin/work-orders").param("status", "DELIVERED"))
				.andExpect(jsonPath("$.content[*].id", Matchers.hasItem(id)));
		perform(get("/api/admin/reports/average-execution-time"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.averageSeconds").isNumber());
	}

	@Test
	void approvalIsRefusedWhenStockIsInsufficient() throws Exception {
		String id = read(perform(post("/api/admin/work-orders"), """
				{"customerId": "%s", "vehicleId": "%s", "problemDescription": "Troca de filtros"}
				""".formatted(customerId, vehicleId)), "$.id");
		var workOrder = "/api/admin/work-orders/" + id;
		perform(post(workOrder + "/diagnosis"));
		perform(post(workOrder + "/part-items"), partJson(filterId, "6")).andExpect(status().isCreated());
		String trackingCode = read(perform(post(workOrder + "/estimate")), "$.trackingCode");

		mockMvc.perform(post("/api/customer/work-orders/" + trackingCode + "/approval"))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.detail").value(
						"Estoque insuficiente de \"Filtro de óleo\": disponível 5 UN, necessário 6 UN."));

		perform(get(workOrder)).andExpect(jsonPath("$.estimates[0].status").value("PENDING"));
		perform(get("/api/admin/parts/" + filterId))
				.andExpect(jsonPath("$.availableQuantity").value(5))
				.andExpect(jsonPath("$.reservedQuantity").value(0));
	}

	@Test
	void validatesOwnershipAndAccess() throws Exception {
		var otherCustomer = read(perform(post("/api/admin/customers"), """
				{"name": "Outro", "taxId": "%s"}
				""".formatted(TestData.randomCpf())), "$.id");
		perform(post("/api/admin/work-orders"), """
				{"customerId": "%s", "vehicleId": "%s", "problemDescription": "Teste"}
				""".formatted(otherCustomer, vehicleId))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.detail").value("O veículo informado não pertence ao cliente."));

		mockMvc.perform(get("/api/customer/work-orders/ABCDEFGHJKLM")).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/customer/work-orders/codigo")).andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/admin/work-orders")).andExpect(status().isUnauthorized());

		var mechanic = tokenFor("MECHANIC");
		performAs(mechanic, get("/api/admin/work-orders"), null).andExpect(status().isOk());
		performAs(mechanic, get("/api/admin/reports/average-execution-time"), null)
				.andExpect(status().isForbidden());
	}

	private String registerPart(String prefix, String name, String type, String unit, String price, String stock)
			throws Exception {
		String id = read(perform(post("/api/admin/parts"), """
				{"code": "%s", "name": "%s", "type": "%s", "unitOfMeasure": "%s", "unitPrice": %s, "minimumStock": 1}
				""".formatted(TestData.randomCode(prefix), name, type, unit, price))
				.andExpect(status().isCreated()), "$.id");
		perform(post("/api/admin/parts/" + id + "/entries"), "{\"quantity\": %s}".formatted(stock))
				.andExpect(status().isCreated());
		return id;
	}

	private String laborJson() {
		return "{\"laborOperationId\": \"%s\"}".formatted(laborOperationId);
	}

	private static String partJson(String partId, String quantity) {
		return "{\"partId\": \"%s\", \"quantity\": %s}".formatted(partId, quantity);
	}
}
