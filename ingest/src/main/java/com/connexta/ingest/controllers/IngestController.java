/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.controllers;

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
  public ResponseEntity<Void> ingest(String acceptVersion, MultipartFile multipartFile) {
    String mediaType = multipartFile.getContentType();
    String fileName = multipartFile.getOriginalFilename();
    Long fileSize = multipartFile.getSize();
    log.info("Attempting to ingest %s", fileName);
    String messageTemplate =
        String.format(
            "Unable to %%s for request with params acceptVersion=%s, fileSize=%s, mimeType=%s, fileName=%s",
            acceptVersion, fileSize, mediaType, fileName);
    try {
      ingestService.ingest(fileSize, mediaType, multipartFile.getInputStream(), fileName);
    } catch (StoreException | TransformException e) {
      log.warn(String.format(messageTemplate, "complete ingest"), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (IOException e) {
      log.warn(String.format(messageTemplate, "read file"), e);
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.accepted().build();
  }
}
