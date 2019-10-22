/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.exceptions;

import org.springframework.http.HttpStatus;

public class RetrieveException extends DetailedResponseStatusException {

  protected RetrieveException(HttpStatus status, String reason) {
    super(status, reason);
  }

  public RetrieveException(String reason, Throwable cause) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, reason, cause);
  }

  public RetrieveException(String reason) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, reason);
  }
}
