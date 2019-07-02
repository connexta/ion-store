/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.controllers;

import com.connexta.multiintstore.common.StorageManager;
import com.connexta.multiintstore.common.exceptions.StorageException;
import com.connexta.multiintstore.services.api.StoreApi;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.URL;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/mis")
public class StoreController implements StoreApi {

  @NotNull private final StorageManager storageManager;

  public StoreController(@NotNull final StorageManager storageManager) {
    this.storageManager = storageManager;
  }

  @Override
  public ResponseEntity<Void> storeProduct(
      String acceptVersion,
      Long fileSize,
      String mimeType,
      @Valid MultipartFile file,
      String fileName) {
    try {
      URL location = storageManager.storeProduct(acceptVersion, fileSize, mimeType, file, fileName);
      return new ResponseEntity("\"location\": " + location, HttpStatus.ACCEPTED);
    } catch (IOException | StorageException e) {
      log.error("Unable to store product", e);
      return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping(
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
