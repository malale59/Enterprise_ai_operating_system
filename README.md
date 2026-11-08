# Enterprise Automation Operating System (EAOS)

![CI](https://github.com/malale59/Enterprise_ai_operating_system/actions/workflows/ci.yml/badge.svg)

A secure, self-hosted automation platform built as an operating system of independent modules:
identity, workflow automation, AI services, monitoring, backups, and reporting, all running on
infrastructure the business controls, with a free and open-source stack.

> **Status:** early development. Module 0 (Engineering Foundations) is complete. Module 1
> (Infrastructure) is next. Nothing in this repository is production ready yet.

## Why EAOS

Small and medium businesses often run on disconnected tools (email, spreadsheets, chat, servers)
with staff copying data between them by hand. Enterprise platforms solve this but are priced and
built for large budgets, and SaaS automation tools bill per task in foreign currency and send
customer data through foreign servers.

EAOS targets the gap between them:

- **Self-hosted.** Runs on a single Linux server, with no dependency on a vendor cloud.
- **Free and open source stack.** No licensing fees at any usage level.
- **Data stays on your server.** The AI layer runs locally by default.
- **Modular.** Each capability is an independent module on a shared platform.

## Architecture

EAOS is organised in layers. Each layer only talks to the one beneath it.

```
Presentation Layer        dashboard, admin panel, API docs
Business Logic Layer      Spring Boot controllers: request handling, validation, auth enforcement
Domain Services Layer     business rules (IdentityService, AIService, NotificationService, ...)
Automation Layer          n8n workflows, scheduled tasks, event handlers
Infrastructure Layer      Docker, Ansible, Nginx, Linux
Data Layer                PostgreSQL, file storage, logs, backups
```

Key rules:

- Automation orchestrates, and never contains business rules. Those live in Domain Services.
- The AI provider sits behind an `AIService` interface, so it can be swapped without touching
  the rest of the code.
- Every core table carries an `organization_id` (single tenant now, multi tenant ready).
- Every major technical decision is recorded as an ADR in `docs/decisions/`.

## Technology

| Area | Choice | Status |
|---|---|---|
| Language | Java 21 (LTS) | In use |
| Framework | Spring Boot 4.1 | In use |
| Build | Maven (wrapper included) | In use |
| Formatting | Spotless with google-java-format | In use |
| CI | GitHub Actions | In use |
| Secrets | Ansible Vault | In use |
| Secret scanning | Gitleaks (pre-commit and CI) | In use |
| Database | PostgreSQL with Flyway migrations | Module 1 |
| Containers | Docker, Docker Compose | Module 1 |
| Reverse proxy | Nginx | Module 1 |
| Security | Spring Security, JWT, RBAC | Module 2 |
| Infrastructure automation | Ansible | Module 3 |
| Workflow engine | n8n | Module 4 |
| AI runtime | Ollama (self-hosted) | Module 5 |
| Monitoring | Zabbix, Grafana | Module 6 |

## Modules

| Module | Name | Status |
|---|---|---|
| 0 | Engineering Foundations | Complete |
| 1 | Infrastructure (Docker, PostgreSQL, Nginx) | Next |
| 2 | Identity (users, roles, permissions) | Planned |
| 3 | Infrastructure Automation (Ansible) | Planned |
| 4 | Workflow Engine (n8n) | Planned |
| 5 | AI Services | Planned |
| 6 | Monitoring | Planned |
| 7 | Backup and Recovery | Planned |
| 8 | Executive Dashboard | Planned |
| 9 to 11 | Asset Management, Security Center, Knowledge Base | Planned |

Each module goes through the same lifecycle before the next begins: purpose, requirements,
design, implementation, testing, monitoring, documentation, review.

## Getting started

### Prerequisites

- JDK 21
- Git
- [pre-commit](https://pre-commit.com)
- Ansible (for the vault)

### Setup

```bash
git clone https://github.com/malale59/Enterprise_ai_operating_system.git
cd Enterprise_ai_operating_system

# install the git hooks (formatting, secret scanning, file hygiene)
pre-commit install

# local environment variables
cp .env.example .env        # then replace the placeholder values; .env is gitignored

# build and test the backend
cd backend
./mvnw verify
```

For the encrypted secrets file, follow `ansible/vault/README.md`.

### Useful commands

```bash
./mvnw spotless:apply       # fix formatting
./mvnw spotless:check       # check formatting only
./mvnw verify               # build, run tests, package
pre-commit run --all-files  # run every hook against the whole repo
```

## Project structure

```
.
├── .github/workflows/   CI pipeline
├── ansible/             inventory, playbooks, roles, vault
├── backend/             Spring Boot application (Maven)
├── docs/
│   ├── architecture/    architecture documents
│   ├── decisions/       Architecture Decision Records (ADRs)
│   └── modules/         one document per module
├── docker/              container definitions (Module 1)
├── monitoring/          monitoring configuration (Module 6)
├── nginx/               reverse proxy configuration (Module 1)
├── scripts/             helper scripts
├── workflows/           exported n8n workflows (Module 4)
└── CONTRIBUTING.md
```

Several directories are placeholders for upcoming modules.

## Engineering practices

- **Trunk based development.** `main` is protected, and changes arrive through pull requests.
- **Conventional Commits.** For example `feat(identity): add login endpoint`.
- **Automated gates.** Pre-commit hooks run formatting and secret scanning locally, and CI
  repeats them on every push and pull request.
- **No secrets in Git.** Secrets live in an Ansible Vault file. `.env` is gitignored, and
  Gitleaks scans every commit.
- **Decision records.** The architecture is frozen. Structural changes need a new ADR.

See `CONTRIBUTING.md` for the full working agreement.

## Documentation

- Module documents: `docs/modules/`
- Architecture decisions: `docs/decisions/`
- Vault setup: `ansible/vault/README.md`

## Roadmap

1. Module 1: Docker Compose stack, PostgreSQL, Flyway migrations, Testcontainers, Nginx.
2. Module 2: Spring Security with JWT and role based access control.
3. Modules 3 to 8: automation, workflows, AI services, monitoring, backups, dashboard.

## License

To be decided.
