/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.transform;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.response.DefaultResponseCreator;

public class ExtendedResponseCreator extends DefaultResponseCreator {

  protected ExtendedResponseCreator(HttpStatus statusCode) {
    super(statusCode);
  }
}
