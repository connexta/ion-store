/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.controllers;

import com.connexta.search.rest.spring.IndexApi;
import com.connexta.store.common.IndexManager;
import com.connexta.store.common.exceptions.StorageException;
import java.io.IOException;
import java.io.InputStream;
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
public class IndexController implements IndexApi {

  @NotNull private final IndexManager indexManager;

  public IndexController(@NotNull final IndexManager indexManager) {
    this.indexManager = indexManager;
  }

  /**
   * TODO Use {@link org.springframework.web.server.ResponseStatusException} instead of catching
   * {@link Exception}s
   */
  @Override
  public ResponseEntity<Void> index(
      final String acceptVersion, final String productId, final MultipartFile file) {
    // TODO validate Accept-Version
    // TODO validate productId

    final Long fileSize = file.getSize();
    // TODO validate that fileSize is (0 GB, 10 GB]

    final String mediaType = file.getContentType();
    // TODO verify that mediaType is not blank and is a valid Content Type

    // TODO handle when CST has already been stored

    final InputStream inputStream;
    try {
      inputStream = file.getInputStream();
    } catch (IOException e) {
      log.warn(
          "Unable to read file for index request with params acceptVersion={}, productId={}, mediaType={}",
          acceptVersion,
          productId,
          mediaType,
          e);
      return ResponseEntity.badRequest().build();
    }

    try {
      indexManager.index(productId, mediaType, inputStream);
    } catch (StorageException e) {
      log.warn(
          "Unable to complete index request with params acceptVersion={}, productId={}, metadataType={}}, mediaType={}",
          acceptVersion,
          productId,
          mediaType,
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (UnsupportedOperationException e) {
      return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    return ResponseEntity.ok().build();
  }
}
