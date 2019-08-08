/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.controllers;

import com.connexta.ingest.rest.spring.IngestApi;
import com.connexta.ingest.service.api.IngestService;
import java.io.IOException;
import java.io.InputStream;
import javax.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
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
    String detail = getDescription(acceptVersion, mediaType, fileName, fileSize);
    log.info("Ingest request received {}", detail);
    InputStream inputStream;
    try {
      inputStream = multipartFile.getInputStream();
    } catch (IOException e) {
      throw new ValidationException("Could not open attachment");
    }
    ingestService.ingest(fileSize, mediaType, inputStream, fileName);

    return ResponseEntity.accepted().build();
  }

  private String getDescription(
      String acceptVersion, String mediaType, String fileName, Long fileSize) {
    return String.format(
        "acceptVersion=%s, fileName=%s, fileSize=%d, mediaType=%s",
        acceptVersion, fileName, fileSize, mediaType);
  }
}
