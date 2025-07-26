# CLAUDE.md
 
This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

This is a Spring Boot application that supports both Maven and Gradle build systems:

### Maven Commands
- `./mvnw spring-boot:run` - Run the application in development mode
- `./mvnw package` - Build the JAR file
- `./mvnw test` - Run all tests
- `./mvnw test -Dtest=ClassName` - Run a specific test class
- `./mvnw spring-boot:build-image` - Build container image
- `./mvnw package -P css` - Compile SCSS to CSS (required after SCSS changes)

### Gradle Commands
- `./gradlew bootRun` - Run the application in development mode
- `./gradlew build` - Build the JAR file  
- `./gradlew test` - Run all tests
- `./gradlew test --tests ClassName` - Run a specific test class

## Code Quality and Formatting
- `./mvnw spring-javaformat:apply` (Maven) or `./gradlew format` (Gradle) - Format code according to Spring conventions
- `./mvnw checkstyle:check` (Maven) or `./gradlew check` (Gradle) - Run checkstyle validation

## Database Profiles
The application supports multiple database configurations:
- Default: H2 in-memory database (no profile needed)
- MySQL: `spring.profiles.active=mysql`
- PostgreSQL: `spring.profiles.active=postgres`

Use Docker Compose for external databases:
- `docker compose up mysql` - Start MySQL container
- `docker compose up postgres` - Start PostgreSQL container

## Architecture Overview

This is a classic Spring Boot web application following MVC architecture with the following key packages:

### Core Structure
- **`org.springframework.samples.petclinic`** - Root package containing main application class
- **`model/`** - JPA entities and base classes (`BaseEntity`, `NamedEntity`, `Person`)
- **`owner/`** - Owner and Pet management (entities, controllers, repositories, validators)
- **`vet/`** - Veterinarian management (entities, controllers, repositories)  
- **`system/`** - System configuration, caching, and utility controllers

### Key Architectural Patterns
- **Repository Pattern**: Data access through Spring Data JPA repositories
- **MVC Pattern**: Controllers handle web requests, return Thymeleaf views
- **Entity Validation**: Bean Validation annotations on model classes
- **Caching**: Configured via `CacheConfiguration` using Caffeine
- **Internationalization**: Multiple message bundles in `src/main/resources/messages/`

### Frontend Technology
- **Thymeleaf** templates in `src/main/resources/templates/`
- **Bootstrap 5.3.6** and Font Awesome via WebJars
- **SCSS** compilation from `src/main/scss/` to CSS (requires Maven `css` profile)

### Testing Strategy
- **Integration Tests**: `PetClinicIntegrationTests`, `MySqlIntegrationTests`, `PostgresIntegrationTests`
- **Unit Tests**: Controller and service layer tests using Spring Boot Test
- **Test Applications**: `MysqlTestApplication` and test classes for rapid development feedback
- **Testcontainers**: Used for database integration testing

### Development Notes
- Main application class: `PetClinicApplication.java`
- Supports Spring Boot DevTools for hot reloading
- GraalVM native image support configured
- Uses Spring Boot Actuator for monitoring endpoints
- H2 console available at `/h2-console` in development
