# ADR-012: Flyway migrations stored inside the Spring project

**Status:** Accepted

## Context
The database schema must be created and changed in a repeatable, versioned way. Every module from
Identity onward adds tables, and every core table carries `organization_id` (ADR-010). The
architecture document placed migrations in a top level `database/migrations` folder, before the
backend technology was fixed.

## Alternatives Considered
1. **Init SQL scripts mounted into the Postgres container**
   Rejected. They run only once against an empty data directory, so they cannot apply later
   changes to an existing database.
2. **A top level `database/migrations` folder with a separate migration tool**
   Rejected. It splits the schema from the code that depends on it, and needs a second tool and
   a second step in every environment.
3. **Letting JPA generate the schema automatically (`ddl-auto`)**
   Rejected. Schema changes become invisible, unreviewed, and unpredictable in production.
4. **Liquibase**
   Rejected for now. Capable, but its XML or YAML change format adds a layer, and Flyway's plain
   SQL files are easier to read and review.
5. **Flyway with SQL scripts inside the Spring Boot project**
   Selected.

## Decision
- Flyway applies versioned SQL migrations at application startup.
- Scripts live in `backend/src/main/resources/db/migration`, named `V1__description.sql`,
  `V2__description.sql`, and so on.
- The first migration creates the `identity`, `workflows`, `ai`, and `monitoring` schemas and the
  `identity.organizations` table with its single seed row.
- JPA runs with `ddl-auto=validate`, so it only checks the schema and never changes it.
- Tests run the migrations against a real throwaway PostgreSQL started by Testcontainers.

## Reasoning
- The schema is versioned together with the code that uses it, in the same pull request.
- Plain SQL is explicit, reviewable, and the same in every environment.
- Flyway records applied migrations in its own history table, so each environment knows exactly
  which version it is on.

## Consequences
- **Applied migrations are never edited.** Flyway stores a checksum for each, and a changed file
  stops startup. Fixes are made with a new, higher numbered migration.
- A failing migration stops application startup. That is intentional, because the app must not run
  against a schema it does not understand.
- The `database/migrations` folder from the architecture document is not used. This ADR records
  that structural change.
- Running the tests requires Docker. CI runners provide it.
