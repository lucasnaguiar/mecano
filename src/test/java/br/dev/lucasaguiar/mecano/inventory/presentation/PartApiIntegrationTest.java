package br.dev.lucasaguiar.mecano.inventory.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.dev.lucasaguiar.mecano.ApiIntegrationTest;
import br.dev.lucasaguiar.mecano.TestData;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class PartApiIntegrationTest extends ApiIntegrationTest {

	@Test
	void stockManagerRegistersPartsAndEntries() throws Exception {
		var stockManager = tokenFor("STOCK_MANAGER");
		var code = TestData.randomCode("OLEO");
		var created = performAs(stockManager, post("/api/admin/parts"), """
				{"code": "%s", "name": "Óleo 5W30", "type": "SUPPLY", "unitOfMeasure": "L",
				 "unitPrice": 45.90, "minimumStock": 5}
				""".formatted(code))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.availableQuantity").value(0))
				.andExpect(jsonPath("$.atOrBelowMinimumStock").value(true));
		String id = read(created, "$.id");

		performAs(stockManager, post("/api/admin/parts/" + id + "/entries"), """
				{"quantity": 12.5}
				""")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.availableQuantity").value(12.5))
				.andExpect(jsonPath("$.atOrBelowMinimumStock").value(false));

		performAs(stockManager, put("/api/admin/parts/" + id + "/price"), """
				{"unitPrice": 49.90}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.unitPrice").value(49.90));

		performAs(stockManager, get("/api/admin/parts/" + id + "/movements"), null)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].type").value("ENTRY"))
				.andExpect(jsonPath("$[0].userId").isNotEmpty());

		perform(post("/api/admin/parts"), """
				{"code": "%s", "name": "Duplicado", "type": "SUPPLY", "unitOfMeasure": "L",
				 "unitPrice": 1, "minimumStock": 0}
				""".formatted(code.toLowerCase()))
				.andExpect(status().isConflict());
	}

	@Test
	void listsItemsAtOrBelowMinimum() throws Exception {
		var created = perform(post("/api/admin/parts"), """
				{"code": "%s", "name": "Pastilha de freio", "type": "PART", "unitOfMeasure": "UN",
				 "unitPrice": 89.90, "minimumStock": 4}
				""".formatted(TestData.randomCode("PAST")))
				.andExpect(status().isCreated());
		String id = read(created, "$.id");

		perform(get("/api/admin/parts").param("atOrBelowMinimum", "true"))
				.andExpect(jsonPath("$[*].id", Matchers.hasItem(id)));

		perform(post("/api/admin/parts/" + id + "/entries"), """
				{"quantity": 1.5}
				""")
				.andExpect(status().isBadRequest());

		perform(delete("/api/admin/parts/" + id)).andExpect(status().isNoContent());
		perform(get("/api/admin/parts")).andExpect(jsonPath("$[*].id", Matchers.not(Matchers.hasItem(id))));
	}

	@Test
	void rejectsInvalidInput() throws Exception {
		perform(post("/api/admin/parts"), """
				{"code": "X", "name": "Mangueira", "type": "PART", "unitOfMeasure": "M",
				 "unitPrice": 10, "minimumStock": 0}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Peças são controladas por unidade (UN). Use SUPPLY para insumos."));

		perform(post("/api/admin/parts"), """
				{"code": "X", "name": "Item", "type": "TOOL", "unitOfMeasure": "UN", "unitPrice": 10, "minimumStock": 0}
				""")
				.andExpect(status().isBadRequest());

		var mechanic = tokenFor("MECHANIC");
		performAs(mechanic, get("/api/admin/parts"), null).andExpect(status().isOk());
		performAs(mechanic, post("/api/admin/parts"), "{}").andExpect(status().isForbidden());
	}
}
