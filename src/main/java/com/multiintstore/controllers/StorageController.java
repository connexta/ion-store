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
package com.connexta.multiintstore.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.multiintstore.callbacks.CallbackValidator;
import com.multiintstore.common.StorageManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StorageController {

  private final StorageManager manager = new StorageManager();
  private final CallbackValidator validator = new CallbackValidator();

  @PostMapping(path = "/store/{ingestID}", consumes = "application/json")
  public ResponseEntity store(
      @PathVariable String ingestID, @RequestBody(required = false) JsonNode body) {

    Object callback = validator.parse(body);

    if (callback != null) {
      manager.handleGeneralCallback(callback);
    } else {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    // Todo: Implement path for requesting data from the Transformation Service

    return new ResponseEntity(HttpStatus.OK);
  }
}
