package com.eaos.eaos_backend.identity.service;

import com.eaos.eaos_backend.identity.domain.Permission;
import com.eaos.eaos_backend.identity.dto.RoleResponse;
import com.eaos.eaos_backend.identity.repository.RoleRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

  private final RoleRepository roles;

  public RoleService(RoleRepository roles) {
    this.roles = roles;
  }

  @Transactional(readOnly = true)
  public List<RoleResponse> listRoles() {
    return roles.findAllWithPermissions().stream()
        .map(
            role ->
                new RoleResponse(
                    role.getId(),
                    role.getName(),
                    role.getDescription(),
                    role.getPermissions().stream().map(Permission::getName).sorted().toList()))
        .toList();
  }
}
