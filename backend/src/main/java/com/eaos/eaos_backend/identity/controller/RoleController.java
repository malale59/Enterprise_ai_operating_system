package com.eaos.eaos_backend.identity.controller;

import com.eaos.eaos_backend.identity.dto.RoleResponse;
import com.eaos.eaos_backend.identity.service.RoleService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The controller is thin: it only speaks HTTP. The rules live in the service. */
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

  private final RoleService service;

  public RoleController(RoleService service) {
    this.service = service;
  }

  @GetMapping
  public List<RoleResponse> list() {
    return service.listRoles();
  }
}
