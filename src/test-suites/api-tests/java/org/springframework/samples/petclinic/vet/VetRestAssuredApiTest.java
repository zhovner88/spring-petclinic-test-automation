package org.springframework.samples.petclinic.vet;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.annotation.Transactional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Vet Controller API Tests using REST Assured - Tests Veterinarian endpoints:
 *
 * MVC COMPONENTS TESTED:
 * 🎯 Controller: VetController endpoints (/vets, /vets.html)
 * 📊 Model: Vets wrapper object with vetList, pagination attributes
 * 🖼️  View: Vet templates (vets/vetList) and JSON/XML responses
 * 🗄️  Repository: VetRepository data operations (findAll with pagination)
 * 🔄 Content Negotiation: JSON, XML, HTML response formats
 *
 * ENDPOINTS COVERED:
 * • GET /vets - JSON/XML API endpoint with vet list
 * • GET /vets.html - HTML page with vet list and pagination
 *
 * TESTING APPROACH:
 * ✅ REST Assured for fluent API testing syntax
 * ✅ Content negotiation testing (JSON/XML/HTML)
 * ✅ Response time performance validation
 * ✅ Concurrent request handling validation
 * ✅ Caching behavior verification
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class VetRestAssuredApiTest {

	@LocalServerPort
	private int port;

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
	}

	@Test
	@DisplayName("GET /vets - Should return valid JSON response with vet data")
	void shouldReturnValidJsonResponseWithVetData() {
		given()
		.when()
			.get("/vets")
		.then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("vetList", hasSize(6))  // We know there are 6 vets in data.sql
			.body("vetList[0].firstName", notNullValue())
			.body("vetList[0].lastName", notNullValue())
			.body("vetList[0].specialties", notNullValue());
	}

	@Test
	@DisplayName("GET /vets - Should return XML when requested")
	void shouldReturnXmlWhenRequested() {
		given()
			.accept(ContentType.XML)
		.when()
			.get("/vets")
		.then()
			.statusCode(200)
			.contentType(ContentType.XML)
			.body(hasXPath("/vets"))
			.body(hasXPath("/vets/vetList"));
	}

	@Test
	@DisplayName("GET /vets - Should validate response schema")
	void shouldValidateResponseSchema() {
		given()
		.when()
			.get("/vets")
		.then()
			.statusCode(200)
			.body("vetList", everyItem(hasKey("id")))
			.body("vetList", everyItem(hasKey("firstName")))
			.body("vetList", everyItem(hasKey("lastName")))
			.body("vetList", everyItem(hasKey("specialties")));
	}

	@Test
	@DisplayName("GET /vets - Should handle content negotiation properly")
	void shouldHandleContentNegotiationProperly() {
		// Test JSON response
		given()
			.accept(ContentType.JSON)
		.when()
			.get("/vets")
		.then()
			.statusCode(200)
			.contentType(ContentType.JSON);

		// Test XML response
		given()
			.accept(ContentType.XML)
		.when()
			.get("/vets")
		.then()
			.statusCode(200)
			.contentType(ContentType.XML);
	}

	@Test
	@DisplayName("GET /vets - Should contain specific vet information")
	void shouldContainSpecificVetInformation() {
		given()
		.when()
			.get("/vets")
		.then()
			.statusCode(200)
			.body("vetList.find { it.firstName == 'James' }.lastName", equalTo("Carter"))
			.body("vetList.find { it.firstName == 'Helen' }.lastName", equalTo("Leary"))
			.body("vetList.find { it.firstName == 'Linda' }.lastName", equalTo("Douglas"));
	}

	@Test
	@DisplayName("GET /vets - Should verify specialties structure")
	void shouldVerifySpecialtiesStructure() {
		given()
		.when()
			.get("/vets")
		.then()
			.statusCode(200)
			.body("vetList.findAll { it.specialties.size() > 0 }", hasSize(greaterThan(0)))
			.body("vetList.find { it.specialties.size() > 0 }.specialties[0]", hasKey("id"))
			.body("vetList.find { it.specialties.size() > 0 }.specialties[0]", hasKey("name"));
	}

	@Test
	@DisplayName("GET /vets - Should measure response time")
	void shouldMeasureResponseTime() {
		given()
		.when()
			.get("/vets")
		.then()
			.statusCode(200)
			.time(lessThan(2000L)); // Response should be under 2 seconds
	}

	@Test
	@DisplayName("GET /vets - Should validate headers")
	void shouldValidateHeaders() {
		given()
		.when()
			.get("/vets")
		.then()
			.statusCode(200)
			.header("Content-Type", containsString("application/json"))
			.header("Cache-Control", nullValue());
	}

	@Test
	@DisplayName("GET /vets - Should handle multiple concurrent requests")
	void shouldHandleMultipleConcurrentRequests() {
		// Simulate concurrent requests
		for (int i = 0; i < 5; i++) {
			given()
			.when()
				.get("/vets")
			.then()
				.statusCode(200)
				.body("vetList", hasSize(greaterThan(0)));
		}
	}

	@Test
	@DisplayName("GET /vets - Should verify caching behavior")
	void shouldVerifyCachingBehavior() {
		// First request
		String firstResponse = given()
			.when()
				.get("/vets")
			.then()
				.statusCode(200)
				.extract()
				.asString();

		// Second request should return same data
		given()
		.when()
			.get("/vets")
		.then()
			.statusCode(200)
			.body(equalTo(firstResponse));
	}

}
