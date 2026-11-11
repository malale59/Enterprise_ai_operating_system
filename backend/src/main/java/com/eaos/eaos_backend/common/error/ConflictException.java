package com.eaos.eaos_backend.common.error;

/** Thrown when the request clashes with existing data, for example a used email. HTTP 409. */
public class ConflictException extends RuntimeException {
  public ConflictException(String message) {
    super(message);
  }
}
