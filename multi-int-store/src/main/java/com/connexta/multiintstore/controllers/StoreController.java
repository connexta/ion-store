/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/mis")
public class StoreController {
  @PutMapping(value = "/product/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity storeProduct(
      @RequestHeader("Accept-Version") String acceptVersion,
      @RequestPart("productInfo") JsonNode productInfo,
      @RequestPart("file") MultipartFile file) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Yay, you stored a product!!");
  }

  @PutMapping(
      value = "/product/{productId}/{metadataType}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity storeMetadata(
      @RequestHeader("Accept-Version") String acceptVersion,
      @PathVariable("productId") String productId,
      @PathVariable("metadataType") String metadataType,
      @RequestPart("productInfo") JsonNode productInfo,
      @RequestPart("file") MultipartFile file) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
        .body("Yay, you stored " + metadataType + "!!");
  }
}
