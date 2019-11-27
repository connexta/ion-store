/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.store.clients;

import com.connexta.store.config.TransformConfiguration;
import com.connexta.store.exceptions.common.DetailedResponseStatusException;
import com.connexta.transformation.rest.models.TransformRequest;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
public class TransformClient {

  private final WebClient transformWebClient;

  public TransformClient(WebClient transformWebClient) {
    this.transformWebClient = transformWebClient;
  }

  public URL requestTransform(
      @NotNull final String datasetId,
      @NotNull final URL currentLocation,
      @NotNull final URL finalLocation,
      @NotNull final URL metacardLocation) {
    TransformRequest transformRequest = new TransformRequest();
    transformRequest
        .datasetId(UUID.fromString(datasetId))
        .currentLocation(currentLocation)
        .finalLocation(finalLocation)
        .metacardLocation(metacardLocation);

    ClientResponse response =
        transformWebClient
            .post()
            .uri(TransformConfiguration.TRANSFORM_STATUS_REQUEST_TEMPLATE)
            .body(BodyInserters.fromValue(transformRequest))
            .exchange()
            .block();

    if (response != null) {
      HttpStatus httpStatus = response.statusCode();
      if (HttpStatus.CREATED.equals(httpStatus)) {
        return extractLocation(response);
      }

      log.debug("Received status code {} when sending transform request.", httpStatus);
      throw new DetailedResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Unsuccessful transform request sent.");
    } else {
      throw new DetailedResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "No response received from transform request");
    }
  }

  // all the errors in this method translate into replication not trying again.
  private URL extractLocation(ClientResponse response) {
    URI location = response.headers().asHttpHeaders().getLocation();
    if (location != null) {
      try {
        return location.toURL();
      } catch (MalformedURLException e) {
        throw new DetailedResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Invalid transform status url received from transform response.");
      }
    } else {
      throw new DetailedResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Missing expected transform status url from transform response.");
    }
  }
}
