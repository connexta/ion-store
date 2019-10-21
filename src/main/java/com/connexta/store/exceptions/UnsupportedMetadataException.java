/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.exceptions;

import org.springframework.http.HttpStatus;

public class UnsupportedMetadataException extends CreateProductException {

  public UnsupportedMetadataException(HttpStatus status, String reason) {
    super(status, reason);
  }
}
