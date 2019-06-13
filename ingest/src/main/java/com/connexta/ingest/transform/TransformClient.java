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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Service
public class TransformClient {

  private final RestTemplate restTemplate;
  private final URI transformEndpoint = new URI("TODO");

  @Autowired
  public TransformClient(RestTemplate restTemplate) throws URISyntaxException {
    this.restTemplate = restTemplate;
  }

  public TransformResponse requestTransform(TransformRequest transformRequest) {
    ResponseErrorHandler originalRequestHandler = restTemplate.getErrorHandler();
    try {
      restTemplate.setErrorHandler(new NoOpResponseErrorHandler());
      return postForTransform(transformRequest);
    } finally {
      restTemplate.setErrorHandler(originalRequestHandler);
    }
  }

  private TransformResponse postForTransform(TransformRequest transformRequest) {
    return buildTransformResponse(sendRequest(transformRequest), transformRequest.getId());
  }

  private TransformResponse buildTransformResponse(
      ResponseEntity<TransformResponse> responseEntity, String id) {
    TransformResponse transformResponse = responseEntity.getBody();
    if (transformResponse == null) {
      transformResponse = new TransformResponse();
      transformResponse.setId(id);
    }
    transformResponse.setStatus(responseEntity.getStatusCode());
    return transformResponse;
  }

  private ResponseEntity<TransformResponse> sendRequest(TransformRequest transformRequest) {
    HttpEntity<TransformRequest> request = new HttpEntity<>(transformRequest);
    return restTemplate.postForEntity(transformEndpoint, request, TransformResponse.class);
  }
}
