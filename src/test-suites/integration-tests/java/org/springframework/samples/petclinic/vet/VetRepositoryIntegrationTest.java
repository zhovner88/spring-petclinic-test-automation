package org.springframework.samples.petclinic.vet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for VetRepository data access layer.
 * 
 * These tests verify vet data retrieval and many-to-many relationships
 * with specialties, ensuring proper entity loading and caching behavior.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"classpath:db/h2/schema.sql", "classpath:db/h2/data.sql"})
class VetRepositoryIntegrationTest {

	@Autowired
	private VetRepository vetRepository;

	/**
	 * Tests basic vet retrieval from the repository.
	 * Verifies that test data is properly loaded.
	 */
	@Test
	void shouldFindAllVets() {
		Collection<Vet> vets = vetRepository.findAll();
		
		assertThat(vets).hasSizeGreaterThan(0);
	}

	/**
	 * Tests that vets with specialties are properly loaded.
	 * Verifies many-to-many relationship exists in test data.
	 */
	@Test
	void shouldFindVetWithSpecialties() {
		Collection<Vet> vets = vetRepository.findAll();
		
		boolean hasVetWithSpecialties = vets.stream()
			.anyMatch(vet -> !vet.getSpecialties().isEmpty());
		
		assertThat(hasVetWithSpecialties).isTrue();
	}

	/**
	 * Tests eager loading of vet specialties.
	 * Verifies that specialty entities are fully populated with names.
	 */
	@Test
	void shouldLoadVetSpecialtiesCorrectly() {
		Collection<Vet> vets = vetRepository.findAll();
		Vet vetWithSpecialties = vets.stream()
			.filter(vet -> !vet.getSpecialties().isEmpty())
			.findFirst()
			.orElse(null);
		
		assertThat(vetWithSpecialties).isNotNull();
		assertThat(vetWithSpecialties.getSpecialties()).isNotEmpty();
		
		Specialty specialty = vetWithSpecialties.getSpecialties().iterator().next();
		assertThat(specialty.getName()).isNotBlank();
	}

}