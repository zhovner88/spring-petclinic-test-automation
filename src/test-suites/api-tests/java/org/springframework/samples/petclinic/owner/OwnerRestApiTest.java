package org.springframework.samples.petclinic.owner;

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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Owner Controller API Tests - Tests Owner management endpoints:
 *
 * MVC COMPONENTS TESTED:
 * 🎯 Controller: OwnerController endpoints (/owners/*, /owners/{id}/*)
 * 📊 Model: Owner entities, pagination attributes (listOwners, currentPage, totalPages)
 * 🖼️  View: Owner templates (ownersList, ownerDetails, createOrUpdateOwnerForm)
 * 🗄️  Repository: OwnerRepository data operations (CRUD, search, pagination)
 * ✅ Validation: Owner entity validation (@Valid, BindingResult)
 *
 * ENDPOINTS COVERED:
 * • GET /owners - List/search owners with pagination
 * • GET /owners/find - Find owners form
 * • GET /owners/new - New owner form
 * • POST /owners/new - Create new owner
 * • GET /owners/{id} - Owner details
 * • GET /owners/{id}/edit - Edit owner form
 * • POST /owners/{id}/edit - Update owner
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"classpath:db/h2/schema.sql", "classpath:db/h2/data.sql"})
@Transactional
class OwnerRestApiTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	@DisplayName("GET /owners - Should return owners search page")
	void shouldReturnOwnersSearchPage() throws Exception {
		// Tests: Controller (OwnerController.processFindForm), Model (pagination + listOwners), View (ownersList)
		mockMvc.perform(get("/owners"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("listOwners"))
			.andExpect(model().attributeExists("currentPage"))
			.andExpect(model().attributeExists("totalPages"))
			.andExpect(model().attributeExists("totalItems"));
	}

	@Test
	@DisplayName("GET /owners/find - Should return find owners page")
	void shouldReturnFindOwnersPage() throws Exception {
		mockMvc.perform(get("/owners/find"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/findOwners"))
			.andExpect(model().attributeExists("owner"));
	}

	@Test
	@DisplayName("GET /owners/new - Should return new owner form")
	void shouldReturnNewOwnerForm() throws Exception {
		mockMvc.perform(get("/owners/new"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/createOrUpdateOwnerForm"))
			.andExpect(model().attributeExists("owner"));
	}

	@Test
	@DisplayName("POST /owners/new - Should create new owner with valid data")
	void shouldCreateNewOwnerWithValidData() throws Exception {
		mockMvc.perform(post("/owners/new")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("firstName", "John")
				.param("lastName", "Doe")
				.param("address", "123 Main St")
				.param("city", "Springfield")
				.param("telephone", "1234567890"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("/owners/*"));
	}

	@Test
	@DisplayName("POST /owners/new - Should reject owner with invalid data")
	void shouldRejectOwnerWithInvalidData() throws Exception {
		mockMvc.perform(post("/owners/new")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("firstName", "")  // Empty first name should fail validation
				.param("lastName", "Doe")
				.param("address", "123 Main St")
				.param("city", "Springfield")
				.param("telephone", "1234567890"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/createOrUpdateOwnerForm"))
			.andExpect(model().hasErrors());
	}

	@Test
	@DisplayName("GET /owners/{ownerId} - Should return owner details for George Franklin")
	void shouldReturnOwnerDetailsForGeorgeFranklin() throws Exception {
		mockMvc.perform(get("/owners/1"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownerDetails"))
			.andExpect(model().attributeExists("owner"))
			.andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
			.andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))));
	}

	@Test
	@DisplayName("GET /owners/{ownerId}/edit - Should return edit form for existing owner")
	void shouldReturnEditFormForExistingOwner() throws Exception {
		mockMvc.perform(get("/owners/1/edit"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/createOrUpdateOwnerForm"))
			.andExpect(model().attributeExists("owner"))
			.andExpect(model().attribute("owner", hasProperty("firstName", is("George"))));
	}

	@Test
	@DisplayName("POST /owners/{ownerId}/edit - Should update existing owner")
	void shouldUpdateExistingOwner() throws Exception {
		mockMvc.perform(post("/owners/1/edit")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("firstName", "UpdatedGeorge")
				.param("lastName", "UpdatedFranklin")
				.param("address", "456 Updated St")
				.param("city", "Updated City")
				.param("telephone", "9876543210"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners/1"));
	}

	@Test
	@DisplayName("GET /owners - Should filter owners by last name")
	void shouldFilterOwnersByLastName() throws Exception {
		// Tests: Controller (OwnerController.processFindForm), Model (listOwners + pagination), Repository (findByLastNameStartingWith), View (ownersList)
		mockMvc.perform(get("/owners")
				.param("lastName", "Davis"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attributeExists("listOwners"))
			.andExpect(model().attributeExists("currentPage"))
			.andExpect(model().attributeExists("totalPages"))
			.andExpect(model().attributeExists("totalItems"))
			.andExpect(model().attribute("currentPage", is(1))) // Default page is 1
			.andExpect(model().attribute("listOwners", hasSize(greaterThan(0)))); // Should have some results
	}

	@Test
	@DisplayName("GET /owners - Should support pagination")
	void shouldSupportPagination() throws Exception {
		mockMvc.perform(get("/owners")
				.param("page", "2")) // Request page 2
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attributeExists("currentPage"))
			.andExpect(model().attributeExists("totalPages"))
			.andExpect(model().attributeExists("totalItems"))
			.andExpect(model().attributeExists("listOwners"))
			.andExpect(model().attribute("currentPage", is(2))) // Should be page 2
			.andExpect(model().attribute("listOwners", hasSize(lessThanOrEqualTo(5)))); // Page size is 5
	}

	@Test
	@DisplayName("GET /owners - Should handle empty search results")
	void shouldHandleEmptySearchResults() throws Exception {
		mockMvc.perform(get("/owners")
				.param("lastName", "NonExistentName"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/findOwners"))
			.andExpect(model().attributeHasFieldErrors("owner", "lastName"));
	}

}
