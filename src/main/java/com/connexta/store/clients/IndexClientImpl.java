/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.clients;

import com.connexta.search.rest.models.IndexRequest;
import com.connexta.store.exceptions.common.DetailedResponseStatusException;
import java.net.URL;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@AllArgsConstructor
public class IndexClientImpl implements IndexClient {

  @NotNull private final WebClient indexWebClient;

  @Override
  public void indexDataset(
      final UUID datasetId, final URL fileUrl, final URL irmUrl, final URL metacardUrl) {
    final IndexRequest indexRequest =
        new IndexRequest()
            .fileLocation(fileUrl.toString())
            .irmLocation(irmUrl.toString())
            .metacardLocation(metacardUrl.toString());

    ClientResponse response =
        indexWebClient
            .put()
            .uri("/index/{datasetId}", datasetId)
            .bodyValue(indexRequest)
            .exchange()
            .block();

    if (response != null) {
      HttpStatus httpStatus = response.statusCode();
      if (!HttpStatus.OK.equals(httpStatus)) {
        log.debug("Index failed with status code {}", httpStatus.value());
        throw new DetailedResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            String.format("Failed to index dataset %s", datasetId));
      }
    } else {
      throw new DetailedResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          String.format("No index response received for dataset %s", datasetId));
    }
  }
}
