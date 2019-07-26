package com.connexta.ingest.common;

import com.connexta.ingest.exceptions.StoreException;
import java.net.URI;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class StreamingRestTemplate<T> {

  private RestTemplate restTemplate = new RestTemplate();

  public StreamingRestTemplate() {
    // Injected RestTemplate has a metrics interceptor which reads the body into memory. Create a
    // new one.
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setBufferRequestBody(false);
    restTemplate.setRequestFactory(requestFactory);
  }

  public T exchange(String endpoint, HttpMethod method, HttpEntity<?> request)
      throws RestClientException {

    ResponseEntity<T> responseEntity;
    responseEntity =
        restTemplate.exchange(
            endpoint, method, request, new ParameterizedTypeReference<T>() {}, (Object) null);
    return responseEntity.getBody();
  }
}
