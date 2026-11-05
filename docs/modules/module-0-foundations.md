# Module 0: Engineering Foundations (Java edition)

## Purpose
Make bad habits structurally impossible before any product code exists: automated formatting,
CI enforcement, secret protection, and environment separation, enforced by tooling.

## Architecture
- Git: trunk-based, protected `main`, PRs required
- Pre-commit: file hygiene, Gitleaks, Spotless (when Java files are staged)
- CI (GitHub Actions): Spotless check, `mvn verify`, Gitleaks on full history
- Secrets: Ansible Vault, password stored outside the repo
- Environments: `.env.example` committed, `.env` gitignored

## Installation
1. Install JDK 21 and Maven; confirm with `java -version` and `mvn -v`.
2. `pip install pre-commit` (or your package manager) then `pre-commit install`.
3. Follow `ansible/vault/README.md` for the vault password.
4. `cp .env.example .env` and fill in generated secrets.

## Configuration
- Build and formatting: `backend/pom.xml`
- CI: `.github/workflows/ci.yml` (folder name is `workflows`, plural)
- Hooks: `.pre-commit-config.yaml`

## Usage
Branch, commit (hooks run), push, open PR, CI runs, merge.
Fix formatting: `mvn -f backend/pom.xml spotless:apply`.

## Testing
1. `mvn -f backend/pom.xml verify` passes locally.
2. A badly formatted Java file makes `spotless:check` fail; `spotless:apply` fixes it.
3. A realistic secret (AWS key pattern) is blocked by Gitleaks at commit time.
4. CI runs green on GitHub, and the run shows the format, build, and secret-scan steps.
5. Vault decrypts with the right password and rejects a wrong one.

## Troubleshooting
- Hook not running: `pre-commit install` is per clone.
- Spotless hook fails with "mvn not found": install Maven or run the check in CI only.
- Gitleaks errors in CI about unknown revision: confirm `fetch-depth: 0` on checkout.

## Future Improvements
Dependabot, commit signing, release automation, and a Docker build step in CI once Module 1
adds the Compose stack.
