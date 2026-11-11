package com.eaos.eaos_backend.identity.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eaos.eaos_backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RoleControllerTests {

  @Autowired MockMvc mvc;

  @Test
  void listsTheThreeSeededRolesWithTheirPermissions() throws Exception {
    mvc.perform(get("/api/v1/roles"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].name").value("ADMIN"))
        .andExpect(jsonPath("$[0].permissions.length()").value(6))
        .andExpect(jsonPath("$[1].name").value("OPERATOR"))
        .andExpect(jsonPath("$[1].permissions.length()").value(2))
        .andExpect(jsonPath("$[2].name").value("VIEWER"))
        .andExpect(jsonPath("$[2].permissions.length()").value(0));
  }

  @Test
  void unknownUrlReturnsAStandardJsonError() throws Exception {
    mvc.perform(get("/api/v1/does-not-exist"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }
}
