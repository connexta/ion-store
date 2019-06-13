/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.endpoint;

import com.connexta.ingest.rest.spring.IngestApi;
import com.connexta.ingest.service.api.IngestService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController()
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

  //  Because ingest is a default method we need to override it
  //  Thankfully however, Spring keeps the magic of annotations
  @Override
  public ResponseEntity<Void> ingest(
      String acceptVersion,
      Long fileSize,
      String mimeType,
      MultipartFile file,
      String title,
      String fileName) {
    final UUID ingestId =
        ingestService.ingest(acceptVersion, fileSize, mimeType, file, title, fileName);
    if (ingestId != null) {
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    // TODO: Send out the Transformation request to process the new data
  }
}
