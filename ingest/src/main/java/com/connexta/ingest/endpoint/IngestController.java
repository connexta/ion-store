/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.endpoint;

import com.connexta.ingest.service.api.IngestRequest;
import com.connexta.ingest.service.api.IngestResponse;
import com.connexta.ingest.service.api.IngestService;
import com.connexta.ingest.service.impl.IngestRequestImpl;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class IngestController {

  private final IngestService ingestService;

  /**
   * Constructs a new REST Spring Web controller.
   *
   * @param ingestService service where requests will be forwarded
   */
  public IngestController(IngestService ingestService) {
    this.ingestService = ingestService;
  }

  @PostMapping(path = "/ingest", consumes = "application/json")
  public ResponseEntity store(@RequestBody(required = false) JsonNode body) {
    IngestRequest request = new IngestRequestImpl(body);
    IngestResponse response = ingestService.ingest(request);
    if (response.isStarted()) {
      return new ResponseEntity(HttpStatus.OK);
    }
    return new ResponseEntity(HttpStatus.BAD_REQUEST);
  }
}
