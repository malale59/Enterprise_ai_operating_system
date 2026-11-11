package com.eaos.eaos_backend.common.error;

/** Thrown when something the client asked for does not exist. Becomes HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
