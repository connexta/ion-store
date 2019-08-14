/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import com.connexta.store.common.ProductStorageManager;
import com.connexta.store.common.exceptions.StorageException;
import com.connexta.store.services.api.StoreApi;
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

  public StoreController(@NotNull final ProductStorageManager productStorageManager) {
    this.productStorageManager = productStorageManager;
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

    final String mediaType = file.getContentType();
    // TODO verify that mediaType is not blank and is a valid Content Type

    final String fileName = file.getOriginalFilename();
    // TODO verify that fileName is not blank

    final InputStream inputStream;
    try {
      inputStream = file.getInputStream();
    } catch (IOException e) {
      log.warn(
          "Unable to read file for storeProduct request with params acceptVersion={}, fileSize={}, mediaType={}, fileName={}",
          acceptVersion,
          fileSize,
          mediaType,
          fileName,
          e);
      return ResponseEntity.badRequest().build();
    }

    final URI location;
    try {
      location = productStorageManager.storeProduct(fileSize, mediaType, fileName, inputStream);
    } catch (StorageException | URISyntaxException e) {
      log.warn(
          "Unable to store product for request with params acceptVersion={}, fileSize={}, mediaType={}, fileName={}",
          acceptVersion,
          fileSize,
          mediaType,
          fileName,
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.created(location).build();
  }
}
