/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.common;

public class RetrievalClientException extends RuntimeException {
  public RetrievalClientException(String message) {
    super(message);
  }

  public RetrievalClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
