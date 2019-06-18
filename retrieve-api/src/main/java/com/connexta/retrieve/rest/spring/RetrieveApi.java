/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.retrieve.rest.spring;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface RetrieveApi {

  @GetMapping(value = "/retrieve/{id}")
  default ResponseEntity<Resource> retrieve(@PathVariable final String id) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }
}
