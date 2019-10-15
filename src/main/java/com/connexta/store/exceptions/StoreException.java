/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.exceptions;

public class StoreException extends Exception {

  public StoreException(String message, Throwable cause) {
    super(message, cause);
  }

  public StoreException(String message) {
    super(message);
  }
}
