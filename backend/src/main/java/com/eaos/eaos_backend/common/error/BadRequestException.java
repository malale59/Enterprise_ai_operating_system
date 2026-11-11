package com.eaos.eaos_backend.common.error;

/** Thrown when the request is invalid in a way annotations cannot express. HTTP 400. */
public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}
