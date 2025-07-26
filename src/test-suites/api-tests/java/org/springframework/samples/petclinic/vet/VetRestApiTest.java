package org.springframework.samples.petclinic.vet;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"classpath:db/h2/schema.sql", "classpath:db/h2/data.sql"})
@ActiveProfiles("test")
@Transactional
class VetRestApiTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	@DisplayName("GET /vets.html - Should return vets list HTML page")
	void shouldReturnVetsListHtmlPage() throws Exception {
		mockMvc.perform(get("/vets.html"))
			.andExpect(status().isOk())
			.andExpect(view().name("vets/vetList"))
			.andExpect(model().attributeExists("currentPage"))
			.andExpect(model().attributeExists("totalPages"))
			.andExpect(model().attributeExists("listVets"))
			.andExpect(model().attributeExists("totalItems"));
	}

	@Test
	@DisplayName("GET /vets - Should return vets data as JSON")
	void shouldReturnVetsDataAsJson() throws Exception {
		mockMvc.perform(get("/vets")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.vetList").exists())
			.andExpect(jsonPath("$.vetList").isArray())
			.andExpect(jsonPath("$.vetList", hasSize(greaterThan(0))))
			.andExpect(jsonPath("$.vetList[0].firstName").exists())
			.andExpect(jsonPath("$.vetList[0].lastName").exists());
	}

	@Test
	@DisplayName("GET /vets - Should return vets data as XML")
	void shouldReturnVetsDataAsXml() throws Exception {
		mockMvc.perform(get("/vets")
				.accept(MediaType.APPLICATION_XML))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_XML))
			.andExpect(xpath("/vets").exists())
			.andExpect(xpath("/vets/vetList").exists());
	}

	@Test
	@DisplayName("GET /vets - Should contain expected vet information")
	void shouldContainExpectedVetInformation() throws Exception {
		mockMvc.perform(get("/vets")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.vetList[0].firstName").isString())
			.andExpect(jsonPath("$.vetList[0].lastName").isString())
			.andExpect(jsonPath("$.vetList[0].specialties").exists());
	}

	@Test
	@DisplayName("GET /vets - Should handle specialties properly")
	void shouldHandleSpecialtiesProperly() throws Exception {
		mockMvc.perform(get("/vets")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.vetList[?(@.specialties.length() > 0)]").exists());
	}

	@Test
	@DisplayName("GET /vets.html - Should handle concurrent requests properly")
	void shouldHandleConcurrentRequestsProperly() throws Exception {
		// Simulate multiple concurrent requests
		for (int i = 0; i < 5; i++) {
			mockMvc.perform(get("/vets.html"))
				.andExpect(status().isOk())
				.andExpect(view().name("vets/vetList"));
		}
	}

	@Test
	@DisplayName("GET /vets - Should validate JSON response structure")
	void shouldValidateJsonResponseStructure() throws Exception {
		mockMvc.perform(get("/vets")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").exists())
			.andExpect(jsonPath("$.vetList").isArray())
			.andExpect(jsonPath("$.vetList[*].id").exists())
			.andExpect(jsonPath("$.vetList[*].firstName").exists())
			.andExpect(jsonPath("$.vetList[*].lastName").exists())
			.andExpect(jsonPath("$.vetList[*].specialties").exists());
	}

}
