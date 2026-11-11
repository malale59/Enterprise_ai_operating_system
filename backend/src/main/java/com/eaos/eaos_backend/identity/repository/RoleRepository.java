package com.eaos.eaos_backend.identity.repository;

import com.eaos.eaos_backend.identity.domain.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<Role, UUID> {

  @Query("select distinct r from Role r left join fetch r.permissions order by r.name")
  List<Role> findAllWithPermissions();

  Optional<Role> findByOrganizationIdAndName(UUID organizationId, String name);
}
