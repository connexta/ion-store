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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TransformClient {

  @Autowired private RestTemplate restTemplate;

  private String transformEndpoint = "http://TODO";

  public TransformResponse requestTransform(TransformRequest transformRequest)
      throws URISyntaxException {

    return restTemplate.postForObject(
        getTransformEndpoint(), transformRequest, TransformResponse.class);
  }

  public URI getTransformEndpoint() throws URISyntaxException {
    return new URI(transformEndpoint);
  }
}
