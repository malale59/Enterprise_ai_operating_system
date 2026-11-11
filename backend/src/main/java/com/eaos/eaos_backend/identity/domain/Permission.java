package com.eaos.eaos_backend.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** One single action, for example user:create. A fixed catalog changed only by migrations. */
@Entity
@Table(schema = "identity", name = "permissions")
public class Permission {

  @Id private UUID id;

  @Column(nullable = false)
  private String name;

  private String description;

  protected Permission() {}

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }
}
