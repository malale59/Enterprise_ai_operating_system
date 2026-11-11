package com.eaos.eaos_backend.common.api;

import java.util.List;
import org.springframework.data.domain.Page;

/** A stable JSON shape for any paged list, so clients can rely on it. */
public record PageResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages) {

  public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }
}
