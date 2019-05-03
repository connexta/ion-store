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

import com.fasterxml.jackson.databind.JsonNode;
import com.multiintstore.callbacks.CallbackValidator;
import com.multiintstore.callbacks.FinishedCallback;
import com.multiintstore.callbacks.MetadataCallback;
import com.multiintstore.callbacks.ProductCallback;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StorageController {

  @PostMapping(path = "/store/{iid}", consumes = "application/json")
  public ResponseEntity store(
      @PathVariable String iid, @RequestBody(required = false) JsonNode body) {

    CallbackValidator validator = new CallbackValidator();
    Object callback = validator.parse(body);

    if (callback != null) {
      sortCallbacks(callback);
    } else {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    // Todo: Implement path for requesting data from the Transformation Service

    return new ResponseEntity(HttpStatus.OK);
  }

  private void sortCallbacks(Object callback) {
    if (callback instanceof ProductCallback) {
      handleCallback((ProductCallback) callback);
    }
    if (callback instanceof FinishedCallback) {
      handleCallback((FinishedCallback) callback);
    }
    if (callback instanceof MetadataCallback) {
      handleCallback((MetadataCallback) callback);
    }
  }

  private void handleCallback(ProductCallback callback) {
    System.out.println("Product");
    //  TODO :: Check Markings
    //  TODO :: Store Product
  }

  private void handleCallback(MetadataCallback callback) {
    System.out.println("Metadata");
    //  TODO :: Check Markings
    //  TODO :: Store Metadata
  }

  private void handleCallback(FinishedCallback callback) {
    System.out.println("Finished");
    //  TODO :: Remove from Temp-Store
  }
}
