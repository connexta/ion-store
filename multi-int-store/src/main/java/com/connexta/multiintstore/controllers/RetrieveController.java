/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.controllers;

import com.connexta.multiintstore.adaptors.RetrieveResponse;
import com.connexta.multiintstore.common.ProductStorageManager;
import com.connexta.multiintstore.common.exceptions.StorageException;
import com.connexta.multiintstore.services.api.RetrieveApi;
import java.io.IOException;
import java.io.InputStream;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/mis")
public class RetrieveController implements RetrieveApi {

  @NotNull private final ProductStorageManager productStorageManager;

  public RetrieveController(@NotNull ProductStorageManager productStorageManager) {
    this.productStorageManager = productStorageManager;
  }

  @Override
  public @NotNull ResponseEntity<Resource> retrieveProduct(@NotBlank String productId) {
    InputStream inputStream = null;
    try {
      // TODO return 404 if key doesn't exist
      final RetrieveResponse retrieveResponse = productStorageManager.retrieveProduct(productId);
      log.info("Successfully retrieved id={}", productId);

      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentDisposition(
          ContentDisposition.builder("attachment")
              .filename(retrieveResponse.getFileName())
              .build());
      inputStream = retrieveResponse.getInputStream();
      return ResponseEntity.ok()
          .contentType(retrieveResponse.getMediaType())
          .headers(httpHeaders)
          .body(new InputStreamResource(inputStream));
    } catch (StorageException | RuntimeException e) {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException ioe) {
          log.warn("Unable to close InputStream when retrieving key \"{}\".", productId, ioe);
        }
      }

      log.warn("Unable to retrieve {}", productId, e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Throwable t) {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          log.warn("Unable to close InputStream when retrieving key \"{}\".", productId, e);
        }
      }

      throw t;
    }
  }
}
