# ADR-011: Java 21 and Spring Boot for the EAOS backend

**Status:** Accepted

## Context
The EAOS backend needs a language and application framework that can host the Identity module
(authentication, JWT, role-based access control), the Domain Services layer, the integration
points for workflows and AI, and the health and monitoring endpoints. The choice must support
strong typing, a clear layered structure, mature security tooling, reliable database migrations,
and solid automated testing.

## Alternatives Considered
1. **Quarkus**
   Rejected for now. Fast startup and low memory use, but a smaller ecosystem and fewer learning
   resources than Spring Boot.
2. **Micronaut**
   Rejected for now. Similar trade-off to Quarkus: efficient runtime, smaller community.
3. **Jakarta EE without a framework layer**
   Rejected. More boilerplate and configuration for no practical benefit at this scale.
4. **Java 21 with Spring Boot**
   Selected.

## Decision
- Language: Java 21 (LTS)
- Framework: Spring Boot, using the latest stable release at project creation, generated with
  Spring Initializr
- Build tool: Maven, with the Maven wrapper (`mvnw`) committed so builds do not depend on a local
  Maven install
- Initial dependencies: Spring Web, Spring Boot Actuator, Validation, Spring Data JPA, PostgreSQL
  driver, Flyway
- Spring Security is added in Module 2 (Identity), not earlier
- Formatting: Spotless with google-java-format, enforced by a pre-commit hook and in CI
- Testing: JUnit 5, with Testcontainers once the database is introduced
- Database migrations: Flyway, with scripts in `backend/src/main/resources/db/migration`

## Layering
The code maps directly onto the architecture layers:
- `controller`: Business Logic layer (request handling, validation, auth enforcement)
- `service`: Domain Services layer (business rules, such as `IdentityService` and `AIService`)
- `repository`: Data access through Spring Data
- `model`: entities and data contracts

Controllers call services, services call repositories. Business rules never live in controllers.

## Reasoning
- Spring Security is a mature, widely used framework for authentication, JWT, and RBAC, which
  fits the Identity module and the security-first design principle.
- The controller, service, repository structure enforces the separation between request handling
  and business rules that the Domain Services layer requires.
- Spring Boot manages compatible dependency versions through its parent, which keeps builds
  reproducible.
- Flyway gives versioned, repeatable schema changes, which the `organization_id` rule (ADR-010)
  and every later table depend on.
- The ecosystem has extensive documentation, community support, and long-term maintenance.

## Consequences
- **Memory footprint.** A JVM-based service needs more RAM than a minimal runtime. Container
  memory limits and JVM options (for example `-XX:MaxRAMPercentage`) must be set in Docker, and
  real usage must be measured during Module 1. If the numbers are too high for the target
  hardware, native images or a lighter framework are revisited in a new ADR.
- Spring Security is deferred to Module 2 because it secures every endpoint by default, which
  would block the Module 1 health checks.
- Migrations live inside the Spring project instead of a top-level `database/migrations`
  directory. This directory change is recorded when Flyway is introduced.
- The `AIService` interface calls the self-hosted model over HTTP (ADR-007), so the AI layer stays
  independent of the framework.
- Formatting is enforced automatically: a failing `spotless:check` blocks both the commit and the
  CI build.
