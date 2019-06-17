/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.transform;

import com.connexta.transformation.rest.models.TransformRequest;
import com.connexta.transformation.rest.models.TransformResponse;
import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TransformClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransformClient.class);

  @Autowired private RestTemplate restTemplate;

  private URI transformEndpoint;

  @Autowired
  @SuppressWarnings("unused")
  @Value("${endpointUrl.transform}")
  public void setTransformEndpoint(String transformEndpoint) throws URISyntaxException {
    LOGGER.info("Transform endpoint URL is: {}", transformEndpoint);
    this.transformEndpoint = new URI(transformEndpoint);
  }

  public TransformResponse requestTransform(TransformRequest transformRequest) {
    LOGGER.warn("Entering requestTransform {}", transformEndpoint);

    // TODO: Do not hardcode the accept-version value
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept-Version", "0.0.1-SNAPSHOT");

    HttpEntity<TransformRequest> requestEntity = new HttpEntity<>(transformRequest, headers);
    return restTemplate.postForObject(transformEndpoint, requestEntity, TransformResponse.class);
  }

  @SuppressWarnings("unused")
  public void setTransformEndpoint(URI transformEndpoint) {
    this.transformEndpoint = transformEndpoint;
  }
}
