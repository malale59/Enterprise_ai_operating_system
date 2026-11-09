# ADR-013: Soft delete for business entities

**Status:** Accepted

## Context
Identity records are referenced by other data (later: audit logs, workflow history). Accidental
deletions must be recoverable, and it must stay traceable who removed what. Physically deleting
rows breaks references and destroys evidence.

## Alternatives Considered
1. **Hard delete (`DELETE`)**
   Rejected. Not recoverable, and it breaks references to the removed row.
2. **An `is_deleted` boolean**
   Rejected. It says that a row was removed, but not when.
3. **Move deleted rows into archive tables**
   Rejected. Every table needs a twin, and restoring means copying data back.
4. **A `deleted_at` timestamp plus `deleted_by`**
   Selected.

## Decision
- Business entity tables (organizations, users, roles, and later workflows, tasks, assets, and
  similar) carry `deleted_at TIMESTAMPTZ NULL` and, where a user performs the deletion,
  `deleted_by UUID NULL` referencing `identity.users`. NULL means active.
- Exceptions: system catalog tables (`permissions`) and pure link tables (`user_roles`,
  `role_permissions`).
- Unique business keys (email, role name) are enforced with partial unique indexes that apply only
  where `deleted_at IS NULL`.
- Foreign keys never cascade deletes. Rows that others reference are never physically removed.
- Soft-deleted rows are excluded from normal queries by one central mechanism in the entity
  mapping, not by repeating a filter in each query.
- Deleted users cannot authenticate. `enabled = false` is a separate, temporary suspension.
- Permanent removal (purge) is a separate, explicit, administrator-only operation, designed later.

## Reasoning
- Deletions are recoverable and traceable.
- References stay valid, so history never points at a missing row.
- A timestamp records when, which a boolean cannot.
- Partial unique indexes allow a value such as an email to be reused after its owner was deleted.

## Consequences
- Every read must exclude deleted rows. Forgetting this is the classic bug, so it is centralized
  in the mapping and covered by tests. Raw SQL and reports must remember the filter.
- Tables only grow, so a retention and purge plan is needed.
- Deleted rows still contain personal data (for example emails). This conflicts with erasure
  requests and needs a purge or anonymization process. Known gap.
- A reused email can exist once as a deleted row and once as an active row, so audit data must
  reference user ids, never emails.
