# ADR-014: Role based access control data model

**Status:** Accepted

## Context
The platform needs to decide who may do what. The model must fit multi tenancy (ADR-010) and soft
delete (ADR-013), and let permissions change without code changes.

## Alternatives Considered
1. **A single role column on users**
   Rejected. A user cannot hold several roles, and roles cannot be extended.
2. **Roles only, with code checking role names**
   Rejected. Changing what a role may do means changing code.
3. **Permissions assigned directly to each user**
   Rejected. Unmanageable as the number of users grows.
4. **Users, roles, and permissions with many to many links (RBAC)**
   Selected.

## Decision
- Tables in the `identity` schema: `users`, `roles`, `permissions`, `user_roles`,
  `role_permissions`.
- `roles` belong to an organization (`organization_id`). `permissions` are a global catalog named
  `resource:action`, for example `user:create`.
- Link tables use composite primary keys and carry no `organization_id`, because both sides
  already do.
- Code checks permissions, never role names. Roles are named bundles of permissions.
- Starting roles: ADMIN, OPERATOR, VIEWER.
- Roles and permissions are seeded by a migration. Users and passwords are never seeded.

## Reasoning
- What a role can do changes through data, not code.
- Organizations can later define their own roles.
- The catalog of permissions is a contract between the code and the database.

## Consequences
- Revoking a role removes the link row. History of grants will come from the audit log (Security
  Center). Until then it is not kept. Known gap.
- Permission names must be kept in sync between code and migrations.
- Seeding roles must run per organization.
