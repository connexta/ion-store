/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.common;

public class RetrievalServerException extends RuntimeException {
  public RetrievalServerException(String message) {
    super(message);
  }

  public RetrievalServerException(String message, Throwable cause) {
    super(message, cause);
  }
}
