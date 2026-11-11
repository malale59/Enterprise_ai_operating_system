-- V3: the permission catalog and the starting roles (ADR-014). No users, no passwords.

INSERT INTO identity.permissions (name, description) VALUES
    ('user:read',   'View users'),
    ('user:create', 'Create users'),
    ('user:update', 'Change users'),
    ('user:delete', 'Delete (soft delete) users'),
    ('role:read',   'View roles'),
    ('role:assign', 'Give or take roles from users');

INSERT INTO identity.roles (organization_id, name, description)
SELECT o.id, r.name, r.description
FROM identity.organizations o
CROSS JOIN (VALUES
    ('ADMIN',    'Full access to identity management'),
    ('OPERATOR', 'Day to day operations, read only'),
    ('VIEWER',   'Own profile only')
) AS r (name, description)
WHERE o.deleted_at IS NULL;

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM identity.roles r
JOIN (VALUES
    ('ADMIN', 'user:read'), ('ADMIN', 'user:create'), ('ADMIN', 'user:update'),
    ('ADMIN', 'user:delete'), ('ADMIN', 'role:read'), ('ADMIN', 'role:assign'),
    ('OPERATOR', 'user:read'), ('OPERATOR', 'role:read')
) AS m (role_name, permission_name) ON m.role_name = r.name
JOIN identity.permissions p ON p.name = m.permission_name;
