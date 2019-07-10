/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.client;

import com.connexta.ingest.exceptions.TransformException;
import com.connexta.transformation.rest.models.TransformRequest;
import com.connexta.transformation.rest.models.TransformResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class TransformClient {

  @NotNull private final RestTemplate restTemplate;
  @NotEmpty private final String transformEndpoint;

  public TransformClient(
      @NotNull RestTemplate restTemplate,
      @Qualifier("transformEndpoint") @NotEmpty String transformEndpoint) {
    this.restTemplate = restTemplate;
    this.transformEndpoint = transformEndpoint;
    log.info("Transformation Service URL: {}", transformEndpoint);
  }

  public void requestTransform(
      final Long fileSize, @NotEmpty final String mimeType, @NotEmpty final String location)
      throws TransformException {
    final HttpHeaders headers = new HttpHeaders();
    // TODO: Do not hardcode the accept-version value
    headers.set("Accept-Version", "0.0.1-SNAPSHOT");

    final TransformRequest transformRequest = new TransformRequest();
    transformRequest.setBytes(fileSize);
    transformRequest.setMimeType(mimeType);
    transformRequest.setProductLocation(location);

    final HttpEntity<TransformRequest> requestEntity = new HttpEntity<>(transformRequest, headers);
    log.info("Transformation requestEntity: {}", requestEntity.toString());

    try {
      restTemplate.postForObject(transformEndpoint, requestEntity, TransformResponse.class);
    } catch (RuntimeException e) {
      throw new TransformException("Unable to complete transform request", e);
    }
  }
}
