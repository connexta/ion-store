/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.endpoint;

import com.connexta.ingest.rest.spring.IngestApi;
import com.connexta.ingest.service.api.IngestService;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController()
public class IngestController implements IngestApi {

  private final IngestService ingestService;
  private static Logger LOGGER = LoggerFactory.getLogger(IngestController.class);

  /**
   * Constructs a new REST Spring Web controller.
   *
   * @param ingestService service where requests will be forwarded
   */
  public IngestController(IngestService ingestService) {
    this.ingestService = ingestService;
  }

  @Override
  public ResponseEntity<Void> ingest(
      String acceptVersion,
      Long fileSize,
      String mimeType,
      MultipartFile file,
      String title,
      String fileName) {
    LOGGER.info("Attempting to ingest {}", fileName);
    try {
      ingestService.ingest(acceptVersion, fileSize, mimeType, file, title, fileName);
      return new ResponseEntity(HttpStatus.OK);
    } catch (IOException e) {
      LOGGER.warn("Unable to ingest", e);
      return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/retrieve/{ingestId}")
  public ResponseEntity<Resource> retrieve(@PathVariable final String ingestId) {
    LOGGER.info("Attempting to retrieve {}", ingestId);
    // TODO don't use ingestService here
    return ingestService.retrieve(ingestId);
  }
}
