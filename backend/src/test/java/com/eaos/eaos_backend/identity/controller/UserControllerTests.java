package com.eaos.eaos_backend.identity.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eaos.eaos_backend.TestcontainersConfiguration;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UserControllerTests {

  private static final String PASSWORD = "Correct-horse-battery";

  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  @AfterEach
  void cleanUp() {
    jdbc.update(
        "DELETE FROM identity.user_roles WHERE user_id IN "
            + "(SELECT id FROM identity.users WHERE email LIKE 'test-%')");
    jdbc.update("DELETE FROM identity.users WHERE email LIKE 'test-%'");
  }

  private static String uniqueEmail() {
    return "test-" + UUID.randomUUID() + "@example.com";
  }

  private static String json(String email, String password) {
    return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
  }

  /** Creates a user and returns the Location header, the URL of the new user. */
  private String createUser(String email) throws Exception {
    return mvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(email, PASSWORD)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getHeader("Location");
  }

  @Test
  void createReturns201AndNeverExposesThePassword() throws Exception {
    mvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(uniqueEmail(), PASSWORD)))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.enabled").value(true))
        .andExpect(jsonPath("$.roles[0]").value("VIEWER"))
        .andExpect(jsonPath("$.password").doesNotExist())
        .andExpect(jsonPath("$.passwordHash").doesNotExist());
  }

  @Test
  void passwordIsStoredAsABcryptHash() throws Exception {
    String email = uniqueEmail();
    createUser(email);
    String stored =
        jdbc.queryForObject(
            "SELECT password_hash FROM identity.users WHERE email = ?", String.class, email);
    assertTrue(stored.startsWith("{bcrypt}$2"));
    assertFalse(stored.contains(PASSWORD));
  }

  @Test
  void shortPasswordReturns400AndNamesTheField() throws Exception {
    mvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(uniqueEmail(), "short")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("password"));
  }

  @Test
  void invalidEmailReturns400AndNamesTheField() throws Exception {
    mvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("not-an-email", PASSWORD)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("email"));
  }

  @Test
  void duplicateEmailReturns409EvenWithDifferentCapitals() throws Exception {
    String email = uniqueEmail();
    createUser(email);
    mvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(email.toUpperCase(), PASSWORD)))
        .andExpect(status().isConflict());
  }

  @Test
  void unknownUserReturns404() throws Exception {
    mvc.perform(get("/api/v1/users/" + UUID.randomUUID())).andExpect(status().isNotFound());
  }

  @Test
  void getReturnsTheUser() throws Exception {
    String email = uniqueEmail();
    String location = createUser(email);
    mvc.perform(get(location))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(email));
  }

  @Test
  void patchChangesOnlyTheFieldsThatAreSent() throws Exception {
    String email = uniqueEmail();
    String location = createUser(email);
    mvc.perform(
            patch(location).contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.enabled").value(false))
        .andExpect(jsonPath("$.email").value(email));
  }

  @Test
  void deleteIsSoftAndFreesTheEmail() throws Exception {
    String email = uniqueEmail();
    String location = createUser(email);

    mvc.perform(delete(location)).andExpect(status().isNoContent());
    mvc.perform(get(location)).andExpect(status().isNotFound());

    Integer hidden =
        jdbc.queryForObject(
            "SELECT count(*) FROM identity.users WHERE email = ? AND deleted_at IS NOT NULL",
            Integer.class,
            email);
    assertEquals(1, hidden);

    createUser(email);
  }

  @Test
  void listReturnsAPage() throws Exception {
    createUser(uniqueEmail());
    mvc.perform(get("/api/v1/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.totalElements").isNumber());
  }
}
