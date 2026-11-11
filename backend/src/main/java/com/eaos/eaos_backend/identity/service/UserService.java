package com.eaos.eaos_backend.identity.service;

import com.eaos.eaos_backend.common.api.PageResponse;
import com.eaos.eaos_backend.common.error.BadRequestException;
import com.eaos.eaos_backend.common.error.ConflictException;
import com.eaos.eaos_backend.common.error.ResourceNotFoundException;
import com.eaos.eaos_backend.identity.domain.Organization;
import com.eaos.eaos_backend.identity.domain.Role;
import com.eaos.eaos_backend.identity.domain.User;
import com.eaos.eaos_backend.identity.dto.CreateUserRequest;
import com.eaos.eaos_backend.identity.dto.UpdateUserRequest;
import com.eaos.eaos_backend.identity.dto.UserResponse;
import com.eaos.eaos_backend.identity.repository.OrganizationRepository;
import com.eaos.eaos_backend.identity.repository.RoleRepository;
import com.eaos.eaos_backend.identity.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The rules for users. Controllers speak HTTP; this class decides what is allowed. */
@Service
public class UserService {

  private static final String DEFAULT_ROLE = "VIEWER";
  private static final int BCRYPT_MAX_BYTES = 72;

  private final UserRepository users;
  private final RoleRepository roles;
  private final OrganizationRepository organizations;
  private final PasswordEncoder passwordEncoder;

  public UserService(
      UserRepository users,
      RoleRepository roles,
      OrganizationRepository organizations,
      PasswordEncoder passwordEncoder) {
    this.users = users;
    this.roles = roles;
    this.organizations = organizations;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public UserResponse create(CreateUserRequest request) {
    if (request.password().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
      throw new BadRequestException("Password must be at most 72 bytes");
    }
    String email = normalize(request.email());
    if (users.existsByEmailIgnoreCase(email)) {
      throw new ConflictException("An active user with this email already exists");
    }
    Organization organization =
        organizations
            .findFirstByOrderByCreatedAtAsc()
            .orElseThrow(() -> new IllegalStateException("No organization exists"));

    User user = new User(organization.getId(), email, passwordEncoder.encode(request.password()));
    Role defaultRole =
        roles
            .findByOrganizationIdAndName(organization.getId(), DEFAULT_ROLE)
            .orElseThrow(
                () -> new IllegalStateException("Default role " + DEFAULT_ROLE + " is missing"));
    user.getRoles().add(defaultRole);
    return toResponse(users.save(user));
  }

  @Transactional(readOnly = true)
  public PageResponse<UserResponse> list(int page, int size) {
    var pageable =
        PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("email"));
    return PageResponse.from(users.findAll(pageable).map(this::toResponse));
  }

  @Transactional(readOnly = true)
  public UserResponse get(UUID id) {
    return toResponse(findActive(id));
  }

  @Transactional
  public UserResponse update(UUID id, UpdateUserRequest request) {
    User user = findActive(id);
    if (request.email() != null) {
      String email = normalize(request.email());
      if (!email.equalsIgnoreCase(user.getEmail()) && users.existsByEmailIgnoreCase(email)) {
        throw new ConflictException("An active user with this email already exists");
      }
      user.setEmail(email);
    }
    if (request.enabled() != null) {
      user.setEnabled(request.enabled());
    }
    return toResponse(user);
  }

  /** Soft delete. Who did it (deleted_by) is filled in once login exists. */
  @Transactional
  public void delete(UUID id) {
    findActive(id).softDelete(null);
  }

  private User findActive(UUID id) {
    return users
        .findActiveById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  private static String normalize(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private UserResponse toResponse(User user) {
    List<String> roleNames = user.getRoles().stream().map(Role::getName).sorted().toList();
    return new UserResponse(
        user.getId(),
        user.getOrganizationId(),
        user.getEmail(),
        user.isEnabled(),
        roleNames,
        user.getCreatedAt(),
        user.getLastLoginAt());
  }
}
