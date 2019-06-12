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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TransformClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransformClient.class);

  @Autowired private RestTemplate restTemplate;

  private com.connexta.transformation.rest.spring.TransformApi transformApi;

  private URI transformEndpoint;

  public TransformResponse requestTransform(TransformRequest transformRequest) {
    LOGGER.warn("Entering requestTransform {}", getTransformEndpoint().toString());
    return restTemplate.postForObject(
        getTransformEndpoint(), transformRequest, TransformResponse.class);
  }

  public URI getTransformEndpoint() {
    return transformEndpoint;
  }

  @SuppressWarnings("unused")
  public void setTransformEndpoint(URI transformEndpoint) {
    this.transformEndpoint = transformEndpoint;
  }

  @SuppressWarnings("unused")
  public void setTransformEndpoint(String transformEndpoint) throws URISyntaxException {
    this.transformEndpoint = new URI(transformEndpoint);
  }
}
