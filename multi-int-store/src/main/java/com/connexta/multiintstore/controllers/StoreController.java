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
import javax.validation.constraints.NotBlank;
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

  /**
   * TODO Use {@link org.springframework.web.server.ResponseStatusException} instead of catching
   * {@link Exception}s
   */
  @Override
  public ResponseEntity<Void> storeProduct(
      @NotBlank final String acceptVersion, @Valid @NotNull final MultipartFile file) {
    // TODO validate Accept-Version

    final Long fileSize = file.getSize();
    // TODO validate that fileSize is (0 GB, 10 GB]

    final String contentType = file.getContentType();
    // TODO verify that contentType is not blank and is a valid Content Type

    final String fileName = file.getOriginalFilename();
    // TODO verify that fileName is not blank

    final InputStream inputStream;
    try {
      inputStream = file.getInputStream();
    } catch (IOException e) {
      log.warn(
          "Unable to read file for storeProduct request with params acceptVersion={}, fileSize={}, contentType={}, fileName={}",
          acceptVersion,
          fileSize,
          contentType,
          fileName,
          e);
      return ResponseEntity.badRequest().build();
    }

    final URI location;
    try {
      location = productStorageManager.storeProduct(fileSize, contentType, fileName, inputStream);
    } catch (StorageException | URISyntaxException e) {
      log.warn(
          "Unable to store product for request with params acceptVersion={}, fileSize={}, contentType={}, fileName={}",
          acceptVersion,
          fileSize,
          contentType,
          fileName,
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.created(location).build();
  }

  /**
   * TODO Use {@link org.springframework.web.server.ResponseStatusException} instead of catching
   * {@link Exception}s
   */
  @Override
  public ResponseEntity<Void> storeMetadata(
      @NotBlank final String acceptVersion,
      @NotBlank final String productId,
      @NotBlank final String metadataType,
      @Valid @NotNull final MultipartFile file) {
    // TODO validate Accept-Version

    // TODO verify that metadataType is not blank

    final Long fileSize = file.getSize();
    // TODO validate that fileSize is (0 GB, 10 GB]

    final String contentType = file.getContentType();
    // TODO verify that contentType is not blank and is a valid Content Type

    // TODO verify id matches something in S3 before storing to solr
    // TODO handle when CST has already been stored

    final InputStream inputStream;
    try {
      inputStream = file.getInputStream();
    } catch (IOException e) {
      log.warn(
          "Unable to read file for storeMetadata request with params acceptVersion={}, productId={}, metadataType={}, contentType={}",
          acceptVersion,
          productId,
          metadataType,
          contentType,
          e);
      return ResponseEntity.badRequest().build();
    }

    try {
      metadataStorageManager.storeMetadata(productId, metadataType, contentType, inputStream);
    } catch (StorageException e) {
      log.warn(
          "Unable to store metadata request with params acceptVersion={}, productId={}, metadataType={}}, contentType={}",
          acceptVersion,
          productId,
          metadataType,
          contentType,
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (UnsupportedOperationException e) {
      return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    return ResponseEntity.ok().build();
  }
}
