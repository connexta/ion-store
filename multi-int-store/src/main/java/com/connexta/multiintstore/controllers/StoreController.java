/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/mis")
public class StoreController {
  @PutMapping(value = "/product", consumes = "*/*")
  public ResponseEntity storeProduct(@RequestBody MultipartFile product) {
    String resourceID = UUID.randomUUID().toString().replace("-", "");
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
        .body("Yay, you stored a product with ID: " + resourceID + "!!");
  }

  @PutMapping(
      value = {"/product/{resourceId}/{metadataType}"},
      consumes = "*/*")
  public ResponseEntity storeMetadata(
      @PathVariable("resourceId") String resourceID,
      @PathVariable(name = "resourceId") String metadataType,
      @RequestBody MultipartFile metadata) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
        .body("Yay, you stored " + metadataType + "!!");
  }
}
