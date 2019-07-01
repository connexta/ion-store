/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.endpoint;

import com.connexta.ingest.exceptions.StorageException;
import com.connexta.ingest.exceptions.TransformException;
import com.connexta.ingest.rest.spring.IngestApi;
import com.connexta.ingest.service.api.IngestService;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController()
public class IngestController implements IngestApi {

  private static Logger LOGGER = LoggerFactory.getLogger(IngestController.class);

  private final IngestService ingestService;

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

    final long actualFileSize = file.getSize();
    if (fileSize != actualFileSize) {
      final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
      LOGGER.info(
          "File size request param ({}) does not match the size of the file ({}). Returning {}.",
          fileSize,
          actualFileSize,
          httpStatus);
      return new ResponseEntity(httpStatus);
    }

    try {
      ingestService.ingest(mimeType, file, fileSize, fileName);
    } catch (IOException | StorageException | TransformException e) {
      LOGGER.warn(
          String.format(
              "Unable to complete ingest request with params acceptVersion=%s, fileSize=%d, mimeType=%s, title=%s, fileName=%s",
              acceptVersion, fileSize, mimeType, title, fileName),
          e);
      return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return new ResponseEntity(HttpStatus.ACCEPTED);
  }
}
