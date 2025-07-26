package org.springframework.samples.petclinic.owner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for OwnerRepository data access layer.
 * 
 * These tests verify repository operations against a real database,
 * focusing on data persistence, query methods, and entity relationships
 * without involving the web layer.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"classpath:db/h2/schema.sql", "classpath:db/h2/data.sql"})
class OwnerRepositoryIntegrationTest {

	@Autowired
	private OwnerRepository ownerRepository;

	/**
	 * Tests the custom query method for finding owners by last name prefix.
	 * Verifies pagination and partial matching functionality.
	 */
	@Test
	void shouldFindOwnersByLastNameStartingWith() {
		Page<Owner> ownersPage = ownerRepository.findByLastNameStartingWith("Davis", PageRequest.of(0, 10));
		List<Owner> owners = ownersPage.getContent();
		
		assertThat(owners).hasSize(2);
		assertThat(owners).allMatch(owner -> owner.getLastName().equals("Davis"));
	}

	/**
	 * Tests basic findAll() repository method.
	 * Ensures test data is properly loaded and accessible.
	 */
	@Test
	void shouldFindAllOwners() {
		List<Owner> owners = ownerRepository.findAll();
		
		assertThat(owners).hasSizeGreaterThan(0);
	}

	/**
	 * Tests findById() method and verifies entity relationships.
	 * Ensures lazy loading of pets collection works correctly.
	 */
	@Test
	void shouldFindOwnerById() {
		Optional<Owner> ownerOptional = ownerRepository.findById(1);
		
		assertThat(ownerOptional).isPresent();
		Owner owner = ownerOptional.get();
		assertThat(owner.getLastName()).isEqualTo("Franklin");
		assertThat(owner.getPets()).isNotEmpty();
	}

	/**
	 * Tests query behavior when no matching records exist.
	 * Verifies empty result handling without exceptions.
	 */
	@Test
	void shouldReturnEmptyPageForNonExistentLastName() {
		Page<Owner> ownersPage = ownerRepository.findByLastNameStartingWith("NonExistent", PageRequest.of(0, 10));
		
		assertThat(ownersPage.getContent()).isEmpty();
	}

}