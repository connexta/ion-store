/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.exceptions;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.springframework.http.HttpStatus;

public class CreateProductException extends DetailedResponseStatusException {

  protected CreateProductException(HttpStatus status, String reason) {
    super(status, reason);
  }

  public CreateProductException(String reason, Throwable cause) {
    super(INTERNAL_SERVER_ERROR, reason, cause);
  }

  public CreateProductException(String reason) {
    super(INTERNAL_SERVER_ERROR, reason);
  }
}
