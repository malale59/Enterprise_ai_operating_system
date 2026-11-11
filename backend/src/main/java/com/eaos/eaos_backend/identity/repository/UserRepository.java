package com.eaos.eaos_backend.identity.repository;

import com.eaos.eaos_backend.identity.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

  /** A query, so the soft delete filter on User always applies. */
  @Query("select u from User u where u.id = :id")
  Optional<User> findActiveById(@Param("id") UUID id);

  boolean existsByEmailIgnoreCase(String email);
}
