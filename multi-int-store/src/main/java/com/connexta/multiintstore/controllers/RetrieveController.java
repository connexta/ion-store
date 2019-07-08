/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.controllers;

import com.connexta.multiintstore.adaptors.RetrieveResponse;
import com.connexta.multiintstore.common.ProductStorageManager;
import com.connexta.multiintstore.services.api.RetrieveApi;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
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
  public ResponseEntity<Resource> retrieveProduct(String productId) {
    final RetrieveResponse retrieveResponse;

    try {
      retrieveResponse = productStorageManager.retrieveProduct(productId);
    } catch (RuntimeException e) {
      log.warn("Unable to retrieve {}", productId, e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    log.info("Successfully retrieved id={}", productId);

    final Resource resource = retrieveResponse.getResource();
    return ResponseEntity.ok()
        .contentType(retrieveResponse.getMediaType())
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + resource.getFilename() + "\"")
        .body(resource);
  }
}
