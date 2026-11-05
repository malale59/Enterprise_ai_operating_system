# Contributing / Working Agreement (EAOS)

Even as a solo project, this repo follows real team discipline.

## Branching
- `main` is protected. Nothing is pushed to it directly.
- Every change happens on a short-lived branch: `feat/identity-login`, `fix/jwt-expiry`.
- Merge via Pull Request, even solo, so CI must pass before merge.

## Commit convention (Conventional Commits)
Format: `type(scope): short description`
Types: `feat`, `fix`, `docs`, `chore`, `refactor`, `test`, `ci`

## Before every commit
Run `pre-commit install` once per clone. Hooks will:
- check file hygiene (whitespace, YAML/XML validity, large files, merge markers)
- scan for secrets with Gitleaks
- check Java formatting with Spotless when .java files are staged

Fix formatting with `mvn -f backend/pom.xml spotless:apply`.
Never bypass hooks with `--no-verify`, and never for the secret scanner.

## Pull Requests
Every PR answers: what changed, why, and how it was tested.
CI must pass (format check, build and tests, secret scan) before merge.

## Architecture changes
The architecture is frozen. Any structural change needs a new ADR in `docs/decisions/`.
