package com.eaos.eaos_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class EaosBackendApplicationTests {

  @Autowired JdbcTemplate jdbc;

  @Test
  void contextLoads() {}

  @Test
  void migrationCreatesSingleDefaultOrganization() {
    Integer count =
        jdbc.queryForObject("SELECT count(*) FROM identity.organizations", Integer.class);
    assertEquals(1, count);
  }
}
