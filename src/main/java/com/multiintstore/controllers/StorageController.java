/*
 * Copyright (c) Connexta
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the
 * GNU Lesser General Public License is distributed along with this
 * program and can be found at http://www.gnu.org/licenses/lgpl.html.
 */
package com.multiintstore.controllers;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StorageController {

  @PostMapping("/store")
  public ResponseEntity store(@RequestBody Map<String, Object> body) {
    // Todo: Implement path for requesting data from the Transformation Service

    // Todo: Implement paths for communicating failures back to the Transformation Service

    return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
