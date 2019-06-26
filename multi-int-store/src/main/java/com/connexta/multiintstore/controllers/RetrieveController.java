/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mis")
public class RetrieveController {
  @GetMapping(value = "/{resourceId}", consumes = "application/json")
  public ResponseEntity retrieveProduct(
      @PathVariable("resourceId") String resourceID,
      @PathVariable(name = "metadataType", required = false) String metadataType) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
        .body("Yay, you retrieved a product with ID: " + resourceID + "!!");
  }

  @GetMapping(
      value = {"/{resourceId}/{metadataType}"},
      consumes = "application/json")
  public ResponseEntity retrieveMetadata(
      @PathVariable("resourceId") String resourceID,
      @PathVariable(name = "resourceId", required = false) String metadataType) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
        .body(
            "Yay, you retrieved "
                + metadataType
                + " for the the product with ID: "
                + resourceID
                + "!!");
  }
}
