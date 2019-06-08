/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.transform;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TransformClient {

  @Autowired private RestTemplate restTemplate;

  private URI transformEndpoint;

  public TransformSuccessResponse requestTransform(TransformRequest transformRequest) {
    HttpEntity<TransformRequest> request = new HttpEntity<>(transformRequest);
    return restTemplate.postForObject(getTransformEndpoint(), request, TransformSuccessResponse.class);
  }

  public URI getTransformEndpoint() {
    return transformEndpoint;
  }

  @SuppressWarnings("unused")
  public void setTransformEndpoint(URI transformEndpoint) {
    this.transformEndpoint = transformEndpoint;
  }

  public void setTransformEndpoint(String transformEndpoint) throws URISyntaxException {
    this.transformEndpoint = new URI(transformEndpoint);
  }
}
