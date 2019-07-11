/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.endpoint;

import com.connexta.ingest.exceptions.StoreException;
import com.connexta.ingest.exceptions.TransformException;
import com.connexta.ingest.rest.spring.IngestApi;
import com.connexta.ingest.service.api.IngestService;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
public class IngestController implements IngestApi {

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
    // TODO validate fileSize

    log.info("Attempting to ingest {}", fileName);

    final Long actualFileSize = file.getSize();
    if (!fileSize.equals(actualFileSize)) {
      log.warn(
          "File size request param ({}) does not match the size of the file ({}).",
          fileSize,
          actualFileSize);
      return ResponseEntity.badRequest().build();
    }

    try {
      ingestService.ingest(fileSize, mimeType, file.getInputStream(), fileName);
    } catch (StoreException | TransformException e) {
      log.warn(
          "Unable to complete ingest request with params acceptVersion={}, fileSize={}, mimeType={}, title={}, fileName={}",
          acceptVersion,
          fileSize,
          mimeType,
          title,
          fileName,
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (IOException e) {
      log.warn(
          "Unable to read file for ingest request with params acceptVersion={}, fileSize={}, mimeType={}, title={}, fileName={}",
          acceptVersion,
          fileSize,
          mimeType,
          title,
          fileName,
          e);
      return ResponseEntity.badRequest().build();
    }

    return ResponseEntity.accepted().build();
  }
}
