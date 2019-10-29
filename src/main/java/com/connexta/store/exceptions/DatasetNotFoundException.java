/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.exceptions;

import org.springframework.http.HttpStatus;

public class DatasetNotFoundException extends RetrieveException {

  public DatasetNotFoundException(String key) {
    super(HttpStatus.NOT_FOUND, String.format("Key %s does not exist", key));
  }
}
