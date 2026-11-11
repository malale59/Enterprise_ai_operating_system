package com.eaos.eaos_backend.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/** The company. Single tenant for now (ADR-010), so users join the first organization. */
@Entity
@Table(schema = "identity", name = "organizations")
@SQLRestriction("deleted_at IS NULL")
public class Organization {

  @Id private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected Organization() {}

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
