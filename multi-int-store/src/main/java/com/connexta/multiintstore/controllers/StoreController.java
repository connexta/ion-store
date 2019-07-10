/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.controllers;

import com.connexta.multiintstore.common.MetadataStorageManager;
import com.connexta.multiintstore.common.ProductStorageManager;
import com.connexta.multiintstore.common.exceptions.StorageException;
import com.connexta.multiintstore.services.api.StoreApi;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/mis")
public class StoreController implements StoreApi {

  @NotNull private final ProductStorageManager productStorageManager;
  @NotNull private final MetadataStorageManager metadataStorageManager;

  public StoreController(
      @NotNull final ProductStorageManager productStorageManager,
      @NotNull final MetadataStorageManager metadataStorageManager) {
    this.productStorageManager = productStorageManager;
    this.metadataStorageManager = metadataStorageManager;
  }

  @Override
  public ResponseEntity<Void> storeProduct(
      @NotEmpty String acceptVersion,
      @NotNull @Min(1L) @Max(10737418240L) Long fileSize,
      @NotEmpty String mimeType,
      @Valid @NotNull MultipartFile file,
      @NotEmpty String fileName) {
    final InputStream inputStream;
    try {
      inputStream = file.getInputStream();
    } catch (IOException e) {
      log.warn(
          "Unable to read file for storeProduct request with params acceptVersion={}, fileSize={}, mimeType={}, fileName={}",
          acceptVersion,
          fileSize,
          mimeType,
          fileName,
          e);
      return ResponseEntity.badRequest().build();
    }

    final URI location;
    try {
      location =
          productStorageManager.storeProduct(
              acceptVersion, fileSize, mimeType, inputStream, fileName);
    } catch (IOException | StorageException | URISyntaxException e) {
      log.warn(
          "Unable to store product for request with params acceptVersion={}, fileSize={}, mimeType={}, fileName={}",
          acceptVersion,
          fileSize,
          mimeType,
          fileName,
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.created(location).build();
  }

  @Override
  public ResponseEntity<Void> storeMetadata(
      @NotEmpty String acceptVersion,
      @NotEmpty String productId,
      @NotEmpty String metadataType,
      @NotNull @Min(1L) @Max(10737418240L) Long fileSize,
      @NotEmpty String mimeType,
      @Valid @NotNull MultipartFile file,
      @NotEmpty String fileName) {
    final InputStream inputStream;
    try {
      inputStream = file.getInputStream();
    } catch (IOException e) {
      log.warn(
          "Unable to read file for storeMetadata request with params acceptVersion={}, productId={}, metadataType={}, fileSize={}, mimeType={}, fileName={}",
          acceptVersion,
          productId,
          metadataType,
          fileSize,
          mimeType,
          fileName,
          e);
      return ResponseEntity.badRequest().build();
    }

    try {
      metadataStorageManager.storeMetadata(
          acceptVersion, productId, metadataType, mimeType, inputStream, fileName);
    } catch (IOException | StorageException e) {
      log.warn(
          "Unable to store metadata request with params acceptVersion={}, productId={}, metadataType={}, fileSize={}, mimeType={}, fileName={}",
          acceptVersion,
          productId,
          metadataType,
          fileSize,
          mimeType,
          fileName,
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
