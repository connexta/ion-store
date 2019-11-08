/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.exceptions;

import com.connexta.store.exceptions.common.DetailedResponseStatusException;
import org.springframework.http.HttpStatus;

public class StoreMetacardException extends DetailedResponseStatusException {

  public StoreMetacardException(String reason, Throwable cause) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, reason, cause);
    addDetail("Caused by " + cause.getMessage());
  }
}
