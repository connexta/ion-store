/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.exceptions;

import org.springframework.http.HttpStatus;

public class RetrieveFileException extends DetailedResponseStatusException {

  protected RetrieveFileException(HttpStatus status, String reason) {
    super(status, reason);
  }

  public RetrieveFileException(String reason, Throwable cause) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, reason, cause);
  }

  public RetrieveFileException(String reason) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, reason);
  }
}
