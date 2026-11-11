package com.eaos.eaos_backend.identity.repository;

import com.eaos.eaos_backend.identity.domain.Organization;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

  Optional<Organization> findFirstByOrderByCreatedAtAsc();
}
