/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.multiintstore.retrieve.impl.spring;

import com.connexta.multiintstore.retrieve.RetrieveService;
import com.connexta.multiintstore.storageadaptor.RetrieveResponse;
import java.io.IOException;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public class RetrieveController {

  private static Logger LOGGER = LoggerFactory.getLogger(RetrieveController.class);

  @NotNull private final RetrieveService retrieveService;

  public RetrieveController(@NotNull RetrieveService retrieveService) {
    this.retrieveService = retrieveService;
  }

  @GetMapping(value = "/retrieve/{id}")
  public ResponseEntity<Resource> retrieve(@PathVariable @NotEmpty final String id) {
    LOGGER.info("Attempting to retrieve {}", id);

    final RetrieveResponse retrieveResponse;

    try {
      retrieveResponse = retrieveService.retrieve(id);
    } catch (IOException e) {
      LOGGER.warn("Unable to retrieve {}", id, e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    final Resource resource = retrieveResponse.getResource();
    // TODO not sure if we need to set the filename in the header
    return ResponseEntity.ok()
        .contentType(retrieveResponse.getMediaType())
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + resource.getFilename() + "\"")
        .body(resource);
  }
}
