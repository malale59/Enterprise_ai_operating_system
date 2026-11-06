# Module 1: Infrastructure

## Purpose
Give the backend a reproducible runtime that any machine can bring up the same way: a Spring Boot
API, a PostgreSQL database, and an Nginx reverse proxy, with a deliberate security boundary and a
clear separation between persistent data and disposable containers.

## Architecture

```
Host
 └── nginx          the only published port (80)
      └── api       Spring Boot, internal network only (8080, not published)
           └── postgres   internal network only, named volume for data
```

- Network: `eaos_internal` (bridge). Containers reach each other by service name.
- Persistence: named volume `postgres_data`. Containers are disposable, the volume is not.
- Schema: Flyway migration `V1` creates the `identity`, `workflows`, `ai`, and `monitoring`
  schemas and `identity.organizations` with one seed row (ADR-010, ADR-012).
- Image: multi-stage Dockerfile. A JDK build stage produces the jar, and a JRE only runtime stage
  runs it as a non-root user.
- Decisions: ADR-010 (organization_id), ADR-011 (Java and Spring Boot), ADR-012 (Flyway).

## Installation
1. Docker and Docker Compose installed (Kali: `docker.io` and `docker-compose` packages; use the
   `docker-compose` command).
2. `cp .env.example .env` and replace the placeholder passwords with generated values.
3. `cp docker-compose.override.yml.example docker-compose.override.yml` for local database access
   (optional, development only).
4. `docker-compose up -d --build --wait`

## Configuration
- `docker-compose.yml`: base file, what production runs. Only Nginx publishes a port.
- `docker-compose.override.yml` (gitignored): publishes Postgres on `127.0.0.1:5432` for local use.
- `backend/src/main/resources/application.properties`: datasource from environment variables,
  `ddl-auto=validate`, only the `health` actuator endpoint exposed.
- `backend/src/main/resources/db/migration/`: Flyway migrations (`V1__...sql`).
- `nginx/nginx.conf`: reverse proxy to `api:8080`, server version hidden.
- JVM: `-XX:MaxRAMPercentage=75` with a 768 MiB container limit on the API.

## Usage
```bash
docker-compose up -d --build --wait     # start everything
docker-compose ps                       # status and published ports
curl http://localhost/actuator/health   # {"status":"UP",...}
docker-compose down                     # stop, keep data
docker-compose down -v                  # stop and delete the database volume
```

## Testing (what was verified, with the real results)

| Check | Result |
|---|---|
| Backend tests (`./mvnw verify`): context loads, migration creates one organization | 2 tests pass, against a real throwaway PostgreSQL (Testcontainers) |
| Same tests in CI (Java 21, GitHub runner) | Pass |
| Stack starts in dependency order | postgres, then api, then nginx, all healthy in about 18 to 23 seconds (warm cache) |
| Request path | `GET /actuator/health` through Nginx returns `UP` |
| API not reachable from the host | Connection to port 8080 refused, no 8080 in `ss -ltn` |
| Database not reachable without the override | Base file only: nothing listening on 5432 |
| Database reachable with the override | Bound to `127.0.0.1:5432` only |
| Container runs as non-root | `uid=999(app)` |
| Only `health` is exposed | `/actuator/env` and `/actuator/beans` return 404 |
| Data persists across `down` and `up` | Row kept, count stays 1 (Flyway does not re-apply V1) |
| Clean rebuild with `down -v` | New volume, V1 re-applied, organization id changed (a new row) |

### Measured resource use (idle, after startup)

| Container | Memory |
|---|---|
| api (Spring Boot) | 257 MiB (limit 768 MiB) |
| postgres | 37 MiB |
| nginx | 5.7 MiB |
| Total | about 300 MiB |

The ADR-011 trade-off (higher memory for a JVM service) is acceptable at this stage. These are idle
figures, so they must be measured again as modules add features and traffic. The self-hosted AI
model, not the Java backend, is expected to be the largest memory consumer later.

## Monitoring
- API: Spring Boot Actuator health endpoint (liveness and readiness groups), used by the Compose
  healthcheck.
- Postgres: `pg_isready` healthcheck.
- Nginx: no healthcheck yet.
- Full monitoring (Zabbix and Grafana) is Module 6.

## Troubleshooting (problems actually hit while building this module)
- **`contextLoads` fails with "Failed to determine a suitable driver class":** database libraries
  were on the classpath with no database configured. Dependencies enter the project in the module
  that needs them, and tests use Testcontainers.
- **Test file cannot find `org.testcontainers...` packages:** the Testcontainers dependencies were
  not in the pom or sat outside `<dependencies>`. Check with `./mvnw dependency:tree`.
- **"Temporary failure in name resolution" during a Maven build:** a DNS or network problem, not a
  code problem. Fix the network and retry with `./mvnw -U verify`.
- **Spotless `NoSuchMethodError` on a newer JDK:** the pinned google-java-format version was too old
  for the JDK. Use a current formatter version and keep local and CI JDKs aligned.
- **Pre-commit "Stashed changes conflicted with hook auto-fixes":** a file was partly staged.
  Stage the whole file (or run `pre-commit run --all-files` first) and commit again.
- **Testcontainers cannot reach Docker:** the shell is not in the `docker` group yet. Log out and
  in, or run `newgrp docker`.
- **Port 80 already in use:** another service on the host holds it. Stop it or change the published
  port in the override file.

## Known gaps (recorded, not forgotten)
- **No TLS.** Nginx serves plain HTTP. TLS needs a real domain and a certificate.
- **Secrets are passed as environment variables.** Anyone with Docker access can read them with
  `docker inspect`. Docker secrets or mounted files are a future ADR.
- **The API connects as the database owner.** A least privilege database role is needed, planned
  with Module 2 and the Security Center.
- **Nginx has no healthcheck**, so `--wait` only confirms that it is running.
- **Memory was measured at idle only.**
- **ADR-011** lists the database dependencies as initial dependencies. In practice they were added
  in this module together with Testcontainers. Accepted ADRs are not edited, so the correction is
  recorded here.

## Future Improvements
- Add a Docker image build step to CI so a broken Dockerfile cannot reach `main`.
- TLS termination in Nginx once a domain exists.
- Docker secrets for credentials, and a restricted database role.
- Rate limiting and security headers in Nginx.
- Re-measure memory at every module, and revisit native images only if the numbers demand it.
