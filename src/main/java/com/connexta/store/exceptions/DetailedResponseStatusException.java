/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.search.common.exceptions;

import java.util.ArrayList;
import java.util.List;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * This class extends on Spring's {@link ResponseStatusException} to provide additional details
 * about the error.
 */
public class DetailedResponseStatusException extends ResponseStatusException implements Detailable {
  private final int code;
  private final List<String> details;
  /**
   * Constructor with a response status.
   *
   * @param status the HTTP status (required)
   */
  public DetailedResponseStatusException(HttpStatus status) {
    this(status, -1);
  }

  /**
   * Constructor with a response status.
   *
   * @param status the HTTP status
   * @param code a more specific code for the error
   */
  public DetailedResponseStatusException(HttpStatus status, int code) {
    super(status);
    this.code = code;
    this.details = new ArrayList<>();
  }

  /**
   * Constructor with a response status and a reason to add to the exception message as explanation.
   *
   * @param status the HTTP status
   * @param reason the associated reason
   */
  public DetailedResponseStatusException(HttpStatus status, @Nullable String reason) {
    this(status, -1, reason);
  }

  /**
   * Constructor with a response status, code, and a reason to add to the exception message as
   * explanation.
   *
   * @param status the HTTP status
   * @param code a more specific code for the error
   * @param reason the associated reason
   */
  public DetailedResponseStatusException(HttpStatus status, int code, @Nullable String reason) {
    super(status, reason);
    this.code = code;
    this.details = new ArrayList<>();
  }

  /**
   * Constructor with a response status and a reason to add to the exception message as explanation,
   * as well as a nested exception.
   *
   * @param status the HTTP status
   * @param reason the associated reason
   * @param cause a nested exception
   */
  public DetailedResponseStatusException(
      HttpStatus status, @Nullable String reason, @Nullable Throwable cause) {
    this(status, -1, reason, cause);
  }

  /**
   * Constructor with a response status, code, and a reason to add to the exception message as
   * explanation, as well as a nested exception.
   *
   * @param status the HTTP status
   * @param code a more specific code for the error
   * @param reason the associated reason
   * @param cause a nested exception
   */
  public DetailedResponseStatusException(
      HttpStatus status, int code, @Nullable String reason, @Nullable Throwable cause) {
    super(status, reason, cause);
    this.code = code;
    this.details = new ArrayList<>();
  }

  @Override
  public int getCode() {
    return code;
  }

  /**
   * Adds the specified detail to be reported in the error message.
   *
   * @param details the details to be added
   * @return this for chaining
   */
  public DetailedResponseStatusException details(String... details) {
    for (final String detail : details) {
      if (detail != null) {
        this.details.add(detail);
      }
    }
    return this;
  }

  /**
   * Adds the specified detail to be reported in the error message.
   *
   * @param detail the detail to be added
   */
  public void addDetail(String detail) {
    details.add(detail);
  }

  @Override
  public List<String> getDetails() {
    return details;
  }

  @Override
  public String getMessage() {
    final String reason = getReason();
    final String codeString = (code != -1) ? " [" + code + "]" : "";
    final String msg = getStatus() + codeString + (reason != null ? " \"" + reason + "\"" : "");

    return NestedExceptionUtils.buildMessage(addDetails(msg), getCause());
  }

  private String addDetails(String message) {
    if (CollectionUtils.isEmpty(details)) {
      return message;
    }
    final StringBuilder sb = new StringBuilder(64);

    if (message != null) {
      sb.append(message).append("; ");
    }
    sb.append("additional details are ").append(details);
    return sb.toString();
  }
}
