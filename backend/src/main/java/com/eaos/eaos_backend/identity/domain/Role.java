package com.eaos.eaos_backend.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/** A named bundle of permissions. Soft deleted rows are hidden from every query (ADR-013). */
@Entity
@Table(schema = "identity", name = "roles")
@SQLRestriction("deleted_at IS NULL")
public class Role {

  @Id private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @ManyToMany
  @JoinTable(
      schema = "identity",
      name = "role_permissions",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  private Set<Permission> permissions = new HashSet<>();

  protected Role() {}

  public UUID getId() {
    return id;
  }

  public UUID getOrganizationId() {
    return organizationId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Set<Permission> getPermissions() {
    return permissions;
  }
}
