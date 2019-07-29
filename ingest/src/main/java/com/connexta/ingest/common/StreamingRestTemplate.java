/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
package com.connexta.ingest.common;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class StreamingRestTemplate {

  private RestTemplate restTemplate = new RestTemplate();
  private String endpoint;
  private HttpMethod httpMethod;

  public StreamingRestTemplate(String endpoint, HttpMethod httpMethod) {
    // Injected RestTemplate has a metrics interceptor which reads the body into memory. Create a
    // new RestTemplate and disable in-memory buggering.
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setBufferRequestBody(false);
    restTemplate.setRequestFactory(requestFactory);
    this.endpoint = endpoint;
    this.httpMethod = httpMethod;
  }

  public <T> T exchange(HttpEntity<?> request, ParameterizedTypeReference<T> returnType)
      throws RestClientException {

    ResponseEntity<T> responseEntity;
    responseEntity = restTemplate.exchange(this.endpoint, this.httpMethod, request, returnType);
    return responseEntity.getBody();
  }
}
