package org.springframework.samples.petclinic;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * API Integration Tests - Tests complete MVC stack:
 * 
 * MVC COMPONENTS TESTED:
 * 🎯 Controller: HTTP request mapping, method invocation
 * 📊 Model: Data attributes passed to views (model.addAttribute)
 * 🖼️  View: Template resolution and view names
 * 🗄️  Repository: Data access through @Sql test data
 * 
 * TESTING APPROACH:
 * ✅ Embedded Spring Boot server starts automatically
 * ✅ Test data loaded using @Sql annotation  
 * ✅ API endpoints work with real data
 * ✅ Full request-response cycle validation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"classpath:db/h2/schema.sql", "classpath:db/h2/data.sql"})
@Transactional
class SimpleWorkingApiTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	@DisplayName("✅ Welcome page should work")
	void welcomePageShouldWork() throws Exception {
		// Tests: Controller mapping (GET /), View resolution (welcome template)
		mockMvc.perform(get("/"))
			.andExpect(status().isOk())
			.andExpect(view().name("welcome"));
	}

	@Test
	@DisplayName("✅ Vets HTML page should load with test data")
	void vetsHtmlPageShouldLoadWithTestData() throws Exception {
		// Tests: Controller (VetController.showVetList), Model (listVets), View (vets/vetList), Repository (VetRepository.findAll)
		mockMvc.perform(get("/vets.html"))
			.andExpect(status().isOk())
			.andExpect(view().name("vets/vetList"))
			.andExpect(model().attributeExists("listVets"))
			.andExpect(model().attribute("listVets", hasSize(5))); // Page size is 5, not 6
	}

	@Test
	@DisplayName("✅ Owner details should work with test data (George Franklin)")
	void ownerDetailsShouldWorkWithTestData() throws Exception {
		// Tests: Controller (OwnerController.showOwner), Model (owner entity), Repository (OwnerRepository.findById), View (ownerDetails)
		mockMvc.perform(get("/owners/1"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownerDetails"))
			.andExpect(model().attributeExists("owner"))
			.andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
			.andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
			.andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
			.andExpect(model().attribute("owner", hasProperty("city", is("Madison"))));
	}

	@Test
	@DisplayName("✅ Create new owner form should work")
	void createNewOwnerFormShouldWork() throws Exception {
		// Tests: Controller (OwnerController.initCreationForm), Model (empty owner object), View (createOrUpdateOwnerForm)
		mockMvc.perform(get("/owners/new"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/createOrUpdateOwnerForm"))
			.andExpect(model().attributeExists("owner"));
	}

	@Test
	@DisplayName("✅ Create new owner should work with valid data")
	void createNewOwnerShouldWorkWithValidData() throws Exception {
		// Tests: Controller (OwnerController.processCreationForm), Validation (@Valid), Repository (OwnerRepository.save), Redirect
		mockMvc.perform(post("/owners/new")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("firstName", "TestFirstName")
				.param("lastName", "TestLastName")
				.param("address", "123 Test St")
				.param("city", "Test City")
				.param("telephone", "1234567890"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("/owners/*"));
	}

	@Test
	@DisplayName("✅ Vets JSON endpoint should return proper data structure")
	void vetsJsonEndpointShouldReturnProperDataStructure() throws Exception {
		// Tests: Controller (VetController.showResourcesVetList), JSON serialization, Repository (VetRepository.findAll), Content negotiation
		mockMvc.perform(get("/vets")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.vetList").exists())
			.andExpect(jsonPath("$.vetList").isArray())
			.andExpect(jsonPath("$.vetList", hasSize(6)))
			.andExpect(jsonPath("$.vetList[0].firstName").exists())
			.andExpect(jsonPath("$.vetList[0].lastName").exists())
			.andExpect(jsonPath("$.vetList[0].specialties").exists());
	}

	@Test
	@DisplayName("✅ Error handling should work - crash controller throws exception")
	void errorHandlingShouldWork() throws Exception {
		// Tests: Controller (CrashController.triggerException), Exception handling in MockMvc environment
		// NOTE: MockMvc doesn't handle exceptions like a real server - it re-throws them as ServletException
		org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
			mockMvc.perform(get("/oups"));
		});
	}

}