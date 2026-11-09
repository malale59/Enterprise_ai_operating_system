package com.eaos.eaos_backend;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class IdentitySchemaTests {

  @Autowired JdbcTemplate jdbc;

  @AfterEach
  void cleanUp() {
    jdbc.update("DELETE FROM identity.users WHERE email LIKE 'test-%'");
  }

  private UUID defaultOrganizationId() {
    return jdbc.queryForObject("SELECT id FROM identity.organizations LIMIT 1", UUID.class);
  }

  private void insertUser(UUID organizationId, String email) {
    jdbc.update(
        "INSERT INTO identity.users (organization_id, email, password_hash) VALUES (?, ?, ?)",
        organizationId,
        email,
        "not-a-real-hash");
  }

  @Test
  void duplicateActiveEmailIsRejected() {
    UUID org = defaultOrganizationId();
    insertUser(org, "test-dup@example.com");
    assertThrows(
        DataIntegrityViolationException.class, () -> insertUser(org, "test-dup@example.com"));
  }

  @Test
  void emailsDifferingOnlyInCaseAreDuplicates() {
    UUID org = defaultOrganizationId();
    insertUser(org, "test-case@example.com");
    assertThrows(
        DataIntegrityViolationException.class, () -> insertUser(org, "TEST-CASE@example.com"));
  }

  @Test
  void emailCanBeReusedAfterSoftDelete() {
    UUID org = defaultOrganizationId();
    insertUser(org, "test-reuse@example.com");
    jdbc.update(
        "UPDATE identity.users SET deleted_at = now() WHERE email = ?", "test-reuse@example.com");
    assertDoesNotThrow(() -> insertUser(org, "test-reuse@example.com"));
  }

  @Test
  void userMustBelongToExistingOrganization() {
    assertThrows(
        DataIntegrityViolationException.class,
        () -> insertUser(UUID.randomUUID(), "test-orphan@example.com"));
  }
}
