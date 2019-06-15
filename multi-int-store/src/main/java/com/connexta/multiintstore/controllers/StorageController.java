/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.controllers;

import com.connexta.multiintstore.callbacks.CallbackValidator;
import com.connexta.multiintstore.common.StorageManager;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StorageController {

  private static final Logger LOGGER = LoggerFactory.getLogger(StorageController.class);
  private final StorageManager manager;
  private final CallbackValidator validator = new CallbackValidator();

  @Autowired
  public StorageController(StorageManager storageManager) {
    this.manager = storageManager;
  }

  @PostMapping(path = "/store/{ingestId}", consumes = "application/json")
  public ResponseEntity store(
      @PathVariable String ingestId, @RequestBody(required = false) JsonNode body) {
    LOGGER.info("Received callback for ingestId {}", ingestId);

    Object callback = validator.parse(body);

    if (callback != null) {
      manager.handleGeneralCallback(callback, ingestId);
    } else {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    // Todo: Implement path for requesting data from the Transformation Service

    return new ResponseEntity(HttpStatus.OK);
  }
}
