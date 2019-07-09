/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.common.exceptions;

public class DuplicateIdException extends RuntimeException {
  public DuplicateIdException(String message) {
    super(message);
  }

  public DuplicateIdException(String message, Throwable cause) {
    super(message, cause);
  }
}
