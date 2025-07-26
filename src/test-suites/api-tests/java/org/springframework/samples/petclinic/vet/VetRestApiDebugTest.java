package org.springframework.samples.petclinic.vet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
 * Debug Tests for Vet Controller - Diagnostic endpoint analysis:
 * 
 * MVC COMPONENTS TESTED:
 * 🎯 Controller: VetController response analysis
 * 📊 Model: Raw response content inspection
 * 🖼️  View: Content type and response format verification
 * 🔍 Debug: Response structure analysis for test development
 * 
 * PURPOSE: These tests help understand actual vs expected responses
 * when developing or debugging other vet-related API tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"classpath:db/h2/schema.sql", "classpath:db/h2/data.sql"})
@Transactional
class VetRestApiDebugTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	@DisplayName("DEBUG: Check what /vets endpoint actually returns")
	void debugVetsEndpoint() throws Exception {
		mockMvc.perform(get("/vets")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(result -> {
				System.out.println("=== DEBUG: Response Content ===");
				System.out.println(result.getResponse().getContentAsString());
				System.out.println("=== DEBUG: Content Type ===");
				System.out.println(result.getResponse().getContentType());
			});
	}

	@Test
	@DisplayName("DEBUG: Check HTML endpoint")
	void debugVetsHtmlEndpoint() throws Exception {
		mockMvc.perform(get("/vets.html"))
			.andExpect(status().isOk())
			.andDo(result -> {
				System.out.println("=== DEBUG: HTML Response ===");
				System.out.println("Content Type: " + result.getResponse().getContentType());
				System.out.println("Content Length: " + result.getResponse().getContentLength());
			});
	}
}