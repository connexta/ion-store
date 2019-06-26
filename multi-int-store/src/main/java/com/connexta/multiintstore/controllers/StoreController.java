/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mis")
public class StoreController {
  @PutMapping(value = "/{resourceId}", consumes = "*/*")
  public ResponseEntity storeProduct(
      @PathVariable("resourceId") String resourceID,
      @PathVariable(name = "metadataType", required = false) String metadataType,
      @RequestBody(required = false) JsonNode body) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
        .body("Yay, you stored a product with ID: " + resourceID + "!!");
  }

  @PutMapping(
      value = {"/{resourceId}/{metadataType}"},
      consumes = "application/json")
  public ResponseEntity storeMetadata(
      @PathVariable("resourceId") String resourceID,
      @PathVariable(name = "resourceId", required = false) String metadataType,
      @RequestBody(required = false) JsonNode body) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
        .body("Yay, you stored " + metadataType + "!!");
  }
}
